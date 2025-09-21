package com.example.service;

import com.example.model.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final AcademicPerformanceRepository academicPerformanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    private static final Map<String, Double> WORK_TYPE_WEIGHTS = Map.of(
            "Экзамен", 3.0,
            "Контрольная работа", 2.0,
            "Урок", 1.0,
            "Домашнее задание", 1.0
    );

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Map<Long, Map<String, Object>> getGradesDashboard(Long studentId) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        List<AcademicPerformance> allGrades = academicPerformanceRepository.findByStudentId(studentId);

        Map<Long, List<AcademicPerformance>> gradesBySubject = allGrades.stream()
                .collect(Collectors.groupingBy(grade -> grade.getSubject().getSubjectId()));

        for (Map.Entry<Long, List<AcademicPerformance>> entry : gradesBySubject.entrySet()) {
            Long subjectId = entry.getKey();
            List<AcademicPerformance> grades = entry.getValue();

            subjectRepository.findById(subjectId).ifPresent(subject -> {
                Map<String, Object> subjectStats = new HashMap<>();
                subjectStats.put("subject", subject);
                subjectStats.put("totalGrades", grades.size());
                subjectStats.put("avgGrade", calculateAverageGrade(grades));
                subjectStats.put("maxGrade", calculateMaxGrade(grades));
                subjectStats.put("minGrade", calculateMinGrade(grades));
                subjectStats.put("grades", grades);
                result.put(subjectId, subjectStats);
            });
        }

        return result;
    }

    public Map<String, Object> getSubjectDetails(Long subjectId, Long studentId) {
        return subjectRepository.findById(subjectId).map(subject -> {
            List<AcademicPerformance> grades = academicPerformanceRepository
                    .findByStudentIdAndSubjectSubjectId(studentId, subjectId);

            Map<String, Object> result = new HashMap<>();
            result.put("subject", subject);
            result.put("grades", grades);
            result.put("avgGrade", calculateAverageGrade(grades));
            result.put("maxGrade", calculateMaxGrade(grades));
            result.put("minGrade", calculateMinGrade(grades));
            result.put("lastGrade", getLastGrade(grades));
            result.put("gradeCounts", countGradesByValue(grades));
            result.put("totalGrades", grades.size());

            return result;
        }).orElse(null);
    }

    private Double calculateAverageGrade(List<AcademicPerformance> grades) {
        if (grades == null || grades.isEmpty()) {
            return null;
        }

        double totalWeightedSum = 0.0;
        double totalWeight = 0.0;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null) {
                Double weight = WORK_TYPE_WEIGHTS.getOrDefault(grade.getWorkType(), 1.0);
                totalWeightedSum += grade.getGrade() * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight == 0) {
            return null;
        }

        return Math.round((totalWeightedSum / totalWeight) * 100.0) / 100.0;
    }

    private Integer calculateMaxGrade(List<AcademicPerformance> grades) {
        return grades.stream()
                .filter(grade -> grade.getGrade() != null)
                .mapToInt(AcademicPerformance::getGrade)
                .max()
                .orElse(0);
    }

    private Integer calculateMinGrade(List<AcademicPerformance> grades) {
        return grades.stream()
                .filter(grade -> grade.getGrade() != null)
                .mapToInt(AcademicPerformance::getGrade)
                .min()
                .orElse(0);
    }

    private Integer getLastGrade(List<AcademicPerformance> grades) {
        return grades.stream()
                .filter(grade -> grade.getGrade() != null)
                .max(Comparator.comparing(AcademicPerformance::getAssessmentDate))
                .map(AcademicPerformance::getGrade)
                .orElse(null);
    }

    private Map<Integer, Integer> countGradesByValue(List<AcademicPerformance> grades) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            counts.put(i, 0);
        }

        if (grades != null) {
            grades.stream()
                    .filter(grade -> grade.getGrade() != null)
                    .forEach(grade -> counts.merge(grade.getGrade(), 1, Integer::sum));
        }

        return counts;
    }

    public List<String> getTeacherNamesBySubjectId(Long subjectId) {
        return teacherSubjectRepository.findBySubjectSubjectId(subjectId).stream()
                .map(ts -> ts.getTeacher().getFullName())
                .collect(Collectors.toList());
    }
}