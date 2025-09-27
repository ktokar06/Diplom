package com.example.service;

import com.example.model.entity.Attendance;
import com.example.model.entity.Subject;
import com.example.repository.AttendanceRepository;
import com.example.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Сервис для управления посещаемостью студентов.
 * Предоставляет функциональность для отслеживания и анализа данных о присутствии на занятиях.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Формирует панель посещаемости студента по всем предметам.
     * Группирует данные посещаемости по предметам и вычисляет статистику присутствий и отсутствий.
     *
     * @param studentId идентификатор студента
     * @return где ключ - ID предмета, значение - карта с данными:
     *         - "subject": объект предмета
     *         - "present": количество присутствий (long)
     *         - "absent": количество отсутствий (long)
     *         - "total": общее количество занятий (long)
     * @throws IllegalArgumentException если studentId равен null
     */
    public Map<Long, Map<String, Object>> getAttendanceDashboard(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }

        List<Attendance> allAttendances = attendanceRepository.findByStudentId(studentId);
        Map<Long, List<Attendance>> groupedBySubject = groupAttendancesBySubject(allAttendances);

        return buildAttendanceDashboard(groupedBySubject);
    }

    /**
     * Группирует записи посещаемости по предметам.
     *
     * @param attendances список записей посещаемости
     * @return записи посещаемости, сгруппированными по ID предметов
     */
    private Map<Long, List<Attendance>> groupAttendancesBySubject(List<Attendance> attendances) {
        Map<Long, List<Attendance>> groupedBySubject = new HashMap<>();

        for (Attendance attendance : attendances) {
            if (attendance.getSubject() != null) {
                Long subjectId = attendance.getSubject().getSubjectId();
                groupedBySubject.computeIfAbsent(subjectId, k -> new ArrayList<>()).add(attendance);
            }
        }

        return groupedBySubject;
    }

    /**
     * Строит панель посещаемости на основе сгруппированных данных.
     *
     * @param groupedBySubject карта с посещаемостью, сгруппированной по предметам
     * @return данные для панели посещаемости
     */
    private Map<Long, Map<String, Object>> buildAttendanceDashboard(Map<Long, List<Attendance>> groupedBySubject) {
        Map<Long, Map<String, Object>> dashboard = new HashMap<>();

        for (Map.Entry<Long, List<Attendance>> entry : groupedBySubject.entrySet()) {
            Long subjectId = entry.getKey();
            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

            if (subjectOpt.isPresent()) {
                Subject subject = subjectOpt.get();
                List<Attendance> attendances = entry.getValue();

                Map<String, Object> subjectData = calculateAttendanceStatistics(attendances);
                subjectData.put("subject", subject);

                dashboard.put(subjectId, subjectData);
            }
        }

        return dashboard;
    }

    /**
     * Вычисляет статистику посещаемости для списка записей.
     *
     * @param attendances список записей посещаемости
     * @return  статистика: present, absent, total
     */
    private Map<String, Object> calculateAttendanceStatistics(List<Attendance> attendances) {
        long present = 0;
        long absent = 0;

        for (Attendance att : attendances) {
            Boolean isPresent = att.getIsPresent();
            if (Boolean.TRUE.equals(isPresent)) {
                present++;
            } else if (Boolean.FALSE.equals(isPresent)) {
                absent++;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("present", present);
        statistics.put("absent", absent);
        statistics.put("total", (long) attendances.size());

        return statistics;
    }

    /**
     * Получает детализированную информацию о посещаемости по конкретному предмету.
     * Включает статистику присутствий, отсутствий, опозданий и процентные соотношения.
     *
     * @param subjectId идентификатор предмета
     * @param studentId идентификатор студента
     * @return детализированные данными о посещаемости или null если предмет не найден:
     *         - "subject": объект предмета
     *         - "attendances": список всех записей посещаемости (отсортированный по дате)
     *         - "presentCount": количество присутствий
     *         - "absentCount": количество отсутствий
     *         - "lateCount": количество опозданий
     *         - "totalClasses": общее количество занятий
     *         - "attendancePercentage": общий процент посещаемости
     *         - "presentPercentage": процент присутствий
     *         - "absentPercentage": процент отсутствий
     *         - "latePercentage": процент опозданий
     * @throws IllegalArgumentException если subjectId или studentId равны null
     */
    public Map<String, Object> getAttendanceDetails(Long subjectId, Long studentId) {
        if (subjectId == null || studentId == null) {
            throw new IllegalArgumentException("Subject ID and Student ID cannot be null");
        }

        Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

        if (subjectOpt.isEmpty()) {
            return null;
        }

        Subject subject = subjectOpt.get();
        List<Attendance> attendances = attendanceRepository
                .findByStudentIdAndSubjectSubjectIdOrderByAttendanceDateDesc(studentId, subjectId);

        return createAttendanceDetails(subject, attendances);
    }

    /**
     * Создает детализированные данные о посещаемости для предмета.
     *
     * @param subject объект предмета
     * @param attendances список записей посещаемости
     * @return детализированные данными
     */
    private Map<String, Object> createAttendanceDetails(Subject subject, List<Attendance> attendances) {
        AttendanceStatistics statistics = calculateDetailedStatistics(attendances);
        int totalCount = attendances.size();

        Map<String, Object> details = new HashMap<>();
        details.put("subject", subject);
        details.put("attendances", attendances);
        details.put("presentCount", statistics.presentCount());
        details.put("absentCount", statistics.absentCount());
        details.put("lateCount", statistics.lateCount());
        details.put("totalClasses", totalCount);
        details.put("attendancePercentage", calculatePercentage(statistics.presentCount(), totalCount));
        details.put("presentPercentage", calculatePercentage(statistics.presentCount(), totalCount));
        details.put("absentPercentage", calculatePercentage(statistics.absentCount(), totalCount));
        details.put("latePercentage", calculatePercentage(statistics.lateCount(), totalCount));

        return details;
    }

    /**
     * Вычисляет детальную статистику посещаемости.
     *
     * @param attendances список записей посещаемости
     * @return объект статистики с подсчитанными показателями
     */
    private AttendanceStatistics calculateDetailedStatistics(List<Attendance> attendances) {
        long presentCount = 0;
        long absentCount = 0;
        long lateCount = 0;

        for (Attendance att : attendances) {
            Boolean isPresent = att.getIsPresent();
            if (Boolean.TRUE.equals(isPresent)) {
                presentCount++;
            } else if (Boolean.FALSE.equals(isPresent)) {
                absentCount++;
            } else {
                lateCount++;
            }
        }

        return new AttendanceStatistics(presentCount, absentCount, lateCount);
    }

    /**
     * Вычисляет процентное соотношение с точностью до двух знаков после запятой.
     * Использует округление HALF_UP для математической точности.
     *
     * @param part часть от общего числа
     * @param total общее число
     * @return процентное значение от 0.0 до 100.0, округленное до 2 знаков
     */
    private double calculatePercentage(long part, int total) {
        if (total <= 0) {
            return 0.0;
        }

        return BigDecimal.valueOf((double) part / total * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Вспомогательная запись для хранения статистики посещаемости.
     */
    private record AttendanceStatistics(long presentCount, long absentCount, long lateCount) {
    }
}