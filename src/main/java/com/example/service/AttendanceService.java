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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;

    public Map<Long, Map<String, Object>> getAttendanceDashboard(Long studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .collect(Collectors.groupingBy(attendance -> attendance.getSubject().getSubjectId()))
                .entrySet().stream()
                .filter(entry -> subjectRepository.findById(entry.getKey()).isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Long subjectId = entry.getKey();
                            List<Attendance> attendances = entry.getValue();
                            Subject subject = subjectRepository.findById(subjectId).orElseThrow();

                            Map<String, Object> subjectStats = new HashMap<>();
                            subjectStats.put("subject", subject);

                            long present = attendances.stream()
                                    .filter(att -> Boolean.TRUE.equals(att.getIsPresent()))
                                    .count();
                            long absent = attendances.stream()
                                    .filter(att -> Boolean.FALSE.equals(att.getIsPresent()))
                                    .count();

                            subjectStats.put("present", present);
                            subjectStats.put("absent", absent);
                            subjectStats.put("total", attendances.size());

                            return subjectStats;
                        }
                ));
    }

    public Map<String, Object> getAttendanceDetails(Long subjectId, Long studentId) {
        return subjectRepository.findById(subjectId).map(subject -> {
            Map<String, Object> result = new HashMap<>();
            result.put("subject", subject);

            List<Attendance> attendances = attendanceRepository
                    .findByStudentIdAndSubjectSubjectIdOrderByAttendanceDateDesc(studentId, subjectId);
            result.put("attendances", attendances);

            long presentCount = attendances.stream()
                    .filter(att -> Boolean.TRUE.equals(att.getIsPresent()))
                    .count();
            long absentCount = attendances.stream()
                    .filter(att -> Boolean.FALSE.equals(att.getIsPresent()))
                    .count();
            long lateCount = attendances.stream()
                    .filter(att -> att.getIsPresent() == null)
                    .count();

            int totalCount = attendances.size();

            result.put("presentCount", presentCount);
            result.put("absentCount", absentCount);
            result.put("lateCount", lateCount);
            result.put("totalClasses", totalCount);

            double attendancePercentage = calculatePercentage(presentCount, totalCount);
            result.put("attendancePercentage", attendancePercentage);

            result.put("presentPercentage", calculatePercentage(presentCount, totalCount));
            result.put("absentPercentage", calculatePercentage(absentCount, totalCount));
            result.put("latePercentage", calculatePercentage(lateCount, totalCount));

            return result;
        }).orElse(null);
    }

    private double calculatePercentage(long part, int total) {
        if (total <= 0) return 0.0;
        return BigDecimal.valueOf((double) part / total * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}