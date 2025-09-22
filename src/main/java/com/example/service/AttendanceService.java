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
 * Сервис для работы с посещаемостью студентов.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Формирует панель посещаемости студента по предметам.
     *
     * @param studentId ID студента.
     * @return карта с данными посещаемости по предметам.
     */
    public Map<Long, Map<String, Object>> getAttendanceDashboard(Long studentId) {
        List<Attendance> allAttendances = attendanceRepository.findByStudentId(studentId);
        Map<Long, List<Attendance>> groupedBySubject = new HashMap<>();


        for (Attendance attendance : allAttendances) {
            Long subjectId = attendance.getSubject().getSubjectId();
            groupedBySubject.computeIfAbsent(subjectId, k -> new ArrayList<>()).add(attendance);
        }

        Map<Long, Map<String, Object>> dashboard = new HashMap<>();
        for (Map.Entry<Long, List<Attendance>> entry : groupedBySubject.entrySet()) {
            Long subjectId = entry.getKey();
            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

            if (subjectOpt.isPresent()) {
                Subject subject = subjectOpt.get();
                List<Attendance> attendances = entry.getValue();

                long present = 0;
                long absent = 0;
                for (Attendance att : attendances) {
                    if (Boolean.TRUE.equals(att.getIsPresent())) {
                        present++;
                    } else if (Boolean.FALSE.equals(att.getIsPresent())) {
                        absent++;
                    }
                }

                Map<String, Object> subjectData = new HashMap<>();
                subjectData.put("subject", subject);
                subjectData.put("present", present);
                subjectData.put("absent", absent);
                subjectData.put("total", attendances.size());

                dashboard.put(subjectId, subjectData);
            }
        }
        return dashboard;
    }

    /**
     * Получает детали посещаемости по конкретному предмету.
     *
     * @param subjectId ID предмета.
     * @param studentId ID студента.
     * @return данные по посещаемости.
     */
    public Map<String, Object> getAttendanceDetails(Long subjectId, Long studentId) {
        Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

        if (subjectOpt.isEmpty()) {
            return null;
        }

        Subject subject = subjectOpt.get();
        List<Attendance> attendances = attendanceRepository
                .findByStudentIdAndSubjectSubjectIdOrderByAttendanceDateDesc(studentId, subjectId);

        long presentCount = 0;
        long absentCount = 0;
        long lateCount = 0;

        for (Attendance att : attendances) {
            if (Boolean.TRUE.equals(att.getIsPresent())) {
                presentCount++;
            } else if (Boolean.FALSE.equals(att.getIsPresent())) {
                absentCount++;
            } else {
                lateCount++;
            }
        }

        int totalCount = attendances.size();
        double attendancePercentage = calculatePercentage(presentCount, totalCount);

        Map<String, Object> details = new HashMap<>();
        details.put("subject", subject);
        details.put("attendances", attendances);
        details.put("presentCount", presentCount);
        details.put("absentCount", absentCount);
        details.put("lateCount", lateCount);
        details.put("totalClasses", totalCount);
        details.put("attendancePercentage", attendancePercentage);
        details.put("presentPercentage", calculatePercentage(presentCount, totalCount));
        details.put("absentPercentage", calculatePercentage(absentCount, totalCount));
        details.put("latePercentage", calculatePercentage(lateCount, totalCount));

        return details;
    }

    /**
     * Вычисляет процентное соотношение.
     *
     * @param part  часть от общего числа.
     * @param total общее число.
     * @return округлённый процент.
     */
    private double calculatePercentage(long part, int total) {
        if (total <= 0) {
            return 0.0;
        }

        return BigDecimal.valueOf((double) part / total * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}