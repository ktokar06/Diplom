package com.example.service;

import com.example.model.Attendance;
import com.example.model.Subject;
import com.example.repository.AttendanceRepository;
import com.example.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Сервис для работы с посещаемостью студентов.
 * Предоставляет методы для расчета статистики посещаемости, построения дашбордов и отчетов.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Получает дашборд с посещаемостью студента по всем предметам с возможностью фильтрации по семестру.
     *
     * @param studentId идентификатор студента
     * @param semester семестр для фильтрации (может быть null)
     * @return структура данных где ключ - ID предмета, значение - данные по посещаемости
     * @throws IllegalArgumentException если studentId равен null
     */
    public Map<Long, Map<String, Object>> getAttendanceDashboard(Long studentId, Integer semester) {
        if (studentId == null) {
            throw new IllegalArgumentException();
        }

        List<Attendance> allAttendances = attendanceRepository.findByStudentId(studentId);

        if (semester != null) {
            List<Attendance> filteredAttendances = new ArrayList<>();
            for (Attendance attendance : allAttendances) {
                if (attendance.getSubject() != null && Objects.equals(semester, attendance.getSubject().getSemester())) {
                    filteredAttendances.add(attendance);
                }
            }
            allAttendances = filteredAttendances;
        }

        Map<Long, List<Attendance>> groupedBySubject = groupAttendancesBySubject(allAttendances);
        return buildAttendanceDashboard(groupedBySubject);
    }

    /**
     * Группирует записи посещаемости по предметам.
     *
     * @param attendances список записей посещаемости
     * @return структура сгруппированных записей по ID предмета
     */
    private Map<Long, List<Attendance>> groupAttendancesBySubject(List<Attendance> attendances) {
        Map<Long, List<Attendance>> groupedBySubject = new HashMap<>();

        for (Attendance attendance : attendances) {
            if (attendance.getSubject() != null) {
                Long subjectId = attendance.getSubject().getSubjectId();
                List<Attendance> subjectAttendances = groupedBySubject.get(subjectId);
                if (subjectAttendances == null) {
                    subjectAttendances = new ArrayList<>();
                    groupedBySubject.put(subjectId, subjectAttendances);
                }
                subjectAttendances.add(attendance);
            }
        }

        return groupedBySubject;
    }

    /**
     * Строит данные дашборда посещаемости на основе сгруппированных записей.
     *
     * @param groupedBySubject сгруппированные записи посещаемости по предметам
     * @return структура данных дашборда посещаемости
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
     * Вычисляет статистику посещаемости по списку записей.
     *
     * @param attendances список записей посещаемости
     * @return структура со статистикой: present, absent, late, total
     */
    private Map<String, Object> calculateAttendanceStatistics(List<Attendance> attendances) {
        long present = 0;
        long absent = 0;
        long late = 0;

        for (Attendance att : attendances) {
            Boolean isPresent = att.getIsPresent();
            if (Boolean.TRUE.equals(isPresent)) {
                present++;
            } else if (Boolean.FALSE.equals(isPresent)) {
                absent++;
            } else {
                late++;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("present", present);
        statistics.put("absent", absent);
        statistics.put("late", late);
        statistics.put("total", (long) attendances.size());

        return statistics;
    }

    /**
     * Получает детализированную информацию по посещаемости для конкретного предмета.
     *
     * @param subjectId идентификатор предмета
     * @param studentId идентификатор студента
     * @param semester семестр для фильтрации
     * @return структура с детализированными данными или null если предмет не найден
     * @throws IllegalArgumentException если subjectId или studentId равны null
     */
    public Map<String, Object> getAttendanceDetails(Long subjectId, Long studentId, Integer semester) {
        if (subjectId == null || studentId == null) {
            throw new IllegalArgumentException();
        }

        Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

        if (subjectOpt.isEmpty()) {
            return null;
        }

        Subject subject = subjectOpt.get();
        List<Attendance> attendances = attendanceRepository
                .findByStudentIdAndSubjectSubjectIdOrderByAttendanceDateDesc(studentId, subjectId);

        if (semester != null && !Objects.equals(semester, subject.getSemester())) {
            attendances = Collections.emptyList();
        }

        return createAttendanceDetails(subject, attendances);
    }

    /**
     * Создает детализированные данные по посещаемости.
     *
     * @param subject предмет
     * @param attendances список записей посещаемости
     * @return структура с детализированными данными
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
     * Вычисляет детализированную статистику посещаемости.
     *
     * @param attendances список записей посещаемости
     * @return объект статистики с количеством присутствий, отсутствий и опозданий
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
     * Вычисляет процентное соотношение.
     *
     * @param part часть от общего количества
     * @param total общее количество
     * @return процентное значение с округлением до 2 знаков после запятой
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