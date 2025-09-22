package com.example.service;

import com.example.model.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Сервис для работы с оценками студентов.
 */
@Service
@RequiredArgsConstructor
public class GradeService {

    private final AcademicPerformanceRepository academicPerformanceRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    /**
     * Веса типов работ для расчёта среднего балла.
     */
    private static final Map<String, Double> WORK_TYPE_WEIGHTS = Map.of(
            "Экзамен", 3.0,
            "Контрольная работа", 2.0,
            "Урок", 1.0,
            "Домашнее задание", 1.0
    );

    /**
     * Формирует панель оценок студента по предметам.
     *
     * @param studentId ID студента.
     * @return карта с данными оценок по предметам.
     */
    public Map<Long, Map<String, Object>> getGradesDashboard(Long studentId) {
        List<AcademicPerformance> performances = academicPerformanceRepository.findByStudentId(studentId);
        Map<Long, List<AcademicPerformance>> groupedBySubject = new HashMap<>();

        for (AcademicPerformance grade : performances) {
            if (grade.getSubject() == null) continue;
            Long subjectId = grade.getSubject().getSubjectId();
            groupedBySubject.computeIfAbsent(subjectId, k -> new ArrayList<>()).add(grade);
        }

        Map<Long, Map<String, Object>> dashboard = new HashMap<>();
        for (Map.Entry<Long, List<AcademicPerformance>> entry : groupedBySubject.entrySet()) {
            Long subjectId = entry.getKey();
            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

            if (subjectOpt.isPresent()) {
                Subject subject = subjectOpt.get();
                List<AcademicPerformance> grades = entry.getValue();

                Map<String, Object> subjectData = new HashMap<>();
                subjectData.put("subject", subject);
                subjectData.put("totalGrades", grades.size());
                subjectData.put("avgGrade", calculateAverageGrade(grades));
                subjectData.put("maxGrade", calculateMaxGrade(grades));
                subjectData.put("minGrade", calculateMinGrade(grades));
                subjectData.put("grades", grades);

                dashboard.put(subjectId, subjectData);
            }
        }

        return dashboard;
    }

    /**
     * Получает детали по конкретному предмету для студента.
     *
     * @param subjectId ID предмета.
     * @param studentId ID студента.
     * @return данные по оценкам по предмету.
     */
    public Map<String, Object> getSubjectDetails(Long subjectId, Long studentId) {
        Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

        if (subjectOpt.isEmpty()) {
            return null;
        }

        Subject subject = subjectOpt.get();
        List<AcademicPerformance> grades = academicPerformanceRepository
                .findByStudentIdAndSubjectSubjectId(studentId, subjectId);

        if (grades == null) {
            grades = new ArrayList<>();
        }

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
    }

    /**
     * Вычисляет среднюю оценку с учётом веса типов работ.
     */
    private Double calculateAverageGrade(List<AcademicPerformance> grades) {
        if (grades == null || grades.isEmpty()) {
            return null;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null) {
                Double weight = WORK_TYPE_WEIGHTS.getOrDefault(grade.getWorkType(), 1.0);
                weightedSum += grade.getGrade() * weight;
                totalWeight += weight;
            }
        }

        if (totalWeight == 0) {
            return null;
        }

        double average = weightedSum / totalWeight;
        return Math.round(average * 100.0) / 100.0;
    }

    /**
     * Находит максимальную оценку.
     */
    private Integer calculateMaxGrade(List<AcademicPerformance> grades) {
        int max = 0;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null && grade.getGrade() > max) {
                max = grade.getGrade();
            }
        }

        return max;
    }

    /**
     * Находит минимальную оценку.
     */
    private Integer calculateMinGrade(List<AcademicPerformance> grades) {
        if (grades == null || grades.isEmpty()) {
            return 0;
        }

        Integer min = null;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null) {
                if (min == null || grade.getGrade() < min) {
                    min = grade.getGrade();
                }
            }
        }

        return min == null ? 0 : min;
    }

    /**
     * Находит последнюю оценку по дате.
     */
    private Integer getLastGrade(List<AcademicPerformance> grades) {
        AcademicPerformance latest = null;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null && grade.getAssessmentDate() != null) {
                if (latest == null || grade.getAssessmentDate().isAfter(latest.getAssessmentDate())) {
                    latest = grade;
                }
            }
        }

        return latest != null ? latest.getGrade() : null;
    }

    /**
     * Подсчитывает количество оценок по значениям (1-5).
     */
    private Map<Integer, Integer> countGradesByValue(List<AcademicPerformance> grades) {
        Map<Integer, Integer> counts = new HashMap<>();

        for (int i = 1; i <= 5; i++) {
            counts.put(i, 0);
        }

        if (grades != null) {
            for (AcademicPerformance grade : grades) {
                if (grade.getGrade() != null && grade.getGrade() >= 1 && grade.getGrade() <= 5) {
                    counts.put(grade.getGrade(), counts.get(grade.getGrade()) + 1);
                }
            }
        }

        return counts;
    }

    /**
     * Получает список имён преподавателей по ID предмета.
     */
    public List<String> getTeacherNamesBySubjectId(Long subjectId) {
        List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findBySubjectSubjectId(subjectId);
        List<String> teacherNames = new ArrayList<>();

        for (TeacherSubject ts : teacherSubjects) {
            Teacher teacher = ts.getTeacher();
            if (teacher != null && teacher.getFullName() != null) {
                teacherNames.add(teacher.getFullName());
            }
        }

        return teacherNames;
    }
}