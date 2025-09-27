package com.example.service;

import com.example.model.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Сервис для управления оценками студентов.
 * Предоставляет функциональность для получения и анализа данных об успеваемости.
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
    private static final Map<String, Double> WORK_TYPE_WEIGHTS = new HashMap<>();

    static {
        WORK_TYPE_WEIGHTS.put("Экзамен", 3.0);
        WORK_TYPE_WEIGHTS.put("Контрольная работа", 2.0);
        WORK_TYPE_WEIGHTS.put("Урок", 1.0);
        WORK_TYPE_WEIGHTS.put("Домашнее задание", 1.0);
    }

    /**
     * Формирует панель оценок студента по всем предметам.
     * Группирует оценки по предметам и вычисляет статистические показатели.
     *
     * @param studentId идентификатор студента
     * @return где ключ - ID предмета, значение - данные:
     *         - "subject": объект предмета
     *         - "totalGrades": общее количество оценок
     *         - "avgGrade": средний балл с учётом весов
     *         - "maxGrade": максимальная оценка
     *         - "minGrade": минимальная оценка
     *         - "grades": список всех оценок по предмету
     * @throws IllegalArgumentException если studentId равен null
     */
    public Map<Long, Map<String, Object>> getGradesDashboard(Long studentId) {
        List<AcademicPerformance> performances = academicPerformanceRepository.findByStudentId(studentId);
        Map<Long, List<AcademicPerformance>> groupedBySubject = groupPerformancesBySubject(performances);

        return buildDashboardData(groupedBySubject);
    }

    /**
     * Группирует оценки по предметам.
     *
     * @param performances список оценок студента
     * @return группированные оценки по ID предметов
     */
    private Map<Long, List<AcademicPerformance>> groupPerformancesBySubject(List<AcademicPerformance> performances) {
        Map<Long, List<AcademicPerformance>> groupedBySubject = new HashMap<>();

        for (AcademicPerformance grade : performances) {
            if (grade.getSubject() != null) {
                Long subjectId = grade.getSubject().getSubjectId();
                List<AcademicPerformance> subjectGrades = groupedBySubject.get(subjectId);
                if (subjectGrades == null) {
                    subjectGrades = new ArrayList<>();
                    groupedBySubject.put(subjectId, subjectGrades);
                }
                subjectGrades.add(grade);
            }
        }

        return groupedBySubject;
    }

    /**
     * Строит данные для панели оценок на основе сгруппированных оценок.
     *
     * @param groupedBySubject оценки сгруппированных по предметам
     * @return данные для панели оценок
     */
    private Map<Long, Map<String, Object>> buildDashboardData(Map<Long, List<AcademicPerformance>> groupedBySubject) {
        Map<Long, Map<String, Object>> dashboard = new HashMap<>();

        for (Map.Entry<Long, List<AcademicPerformance>> entry : groupedBySubject.entrySet()) {
            Long subjectId = entry.getKey();
            Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

            if (subjectOpt.isPresent()) {
                Subject subject = subjectOpt.get();
                List<AcademicPerformance> grades = entry.getValue();

                Map<String, Object> subjectData = createSubjectData(subject, grades);
                dashboard.put(subjectId, subjectData);
            }
        }

        return dashboard;
    }

    /**
     * Создает данные по предмету для панели оценок.
     *
     * @param subject объект предмета
     * @param grades список оценок по предмету
     * @return данные по предмету
     */
    private Map<String, Object> createSubjectData(Subject subject, List<AcademicPerformance> grades) {
        Map<String, Object> subjectData = new HashMap<>();
        subjectData.put("subject", subject);
        subjectData.put("totalGrades", grades.size());
        subjectData.put("avgGrade", calculateAverageGrade(grades));
        subjectData.put("maxGrade", calculateMaxGrade(grades));
        subjectData.put("minGrade", calculateMinGrade(grades));
        subjectData.put("grades", grades);

        return subjectData;
    }

    /**
     * Получает детализированную информацию по конкретному предмету для студента.
     * Включает статистику и распределение оценок.
     *
     * @param subjectId идентификатор предмета
     * @param studentId идентификатор студента
     * @return детализированные данными по предмету или null если предмет не найден:
     *         - "subject": объект предмета
     *         - "grades": список всех оценок
     *         - "avgGrade": средний балл
     *         - "maxGrade": максимальная оценка
     *         - "minGrade": минимальная оценка
     *         - "lastGrade": последняя полученная оценка
     *         - "gradeCounts": распределение оценок по значениям
     *         - "totalGrades": общее количество оценок
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
            grades = Collections.emptyList();
        }

        return createSubjectDetails(subject, grades);
    }

    /**
     * Создает детализированные данные по предмету.
     *
     * @param subject объект предмета
     * @param grades список оценок по предмету
     * @return детализированные данными
     */
    private Map<String, Object> createSubjectDetails(Subject subject, List<AcademicPerformance> grades) {
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
     * Вычисляет средневзвешенную оценку с учётом типов работ.
     * Использует веса из WORK_TYPE_WEIGHTS для расчета.
     *
     * @param grades список оценок для расчета
     * @return средневзвешенный балл, округленный до 2 знаков после запятой, или null если оценки отсутствуют
     */
    private Double calculateAverageGrade(List<AcademicPerformance> grades) {
        if (grades == null || grades.isEmpty()) {
            return null;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null) {
                Double weight = WORK_TYPE_WEIGHTS.get(grade.getWorkType());
                if (weight == null) {
                    weight = 1.0;
                }
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
     * Находит максимальную оценку из списка.
     *
     * @param grades список оценок
     * @return максимальная оценка или 0 если оценки отсутствуют
     */
    private Integer calculateMaxGrade(List<AcademicPerformance> grades) {
        Integer maxGrade = null;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null) {
                if (maxGrade == null || grade.getGrade() > maxGrade) {
                    maxGrade = grade.getGrade();
                }
            }
        }

        return maxGrade != null ? maxGrade : 0;
    }

    /**
     * Находит минимальную оценку из списка.
     *
     * @param grades список оценок
     * @return минимальная оценка или 0 если оценки отсутствуют
     */
    private Integer calculateMinGrade(List<AcademicPerformance> grades) {
        Integer minGrade = null;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null) {
                if (minGrade == null || grade.getGrade() < minGrade) {
                    minGrade = grade.getGrade();
                }
            }
        }

        return minGrade != null ? minGrade : 0;
    }

    /**
     * Находит последнюю оценку по дате получения.
     *
     * @param grades список оценок
     * @return последняя оценка или null если оценки отсутствуют
     */
    private Integer getLastGrade(List<AcademicPerformance> grades) {
        AcademicPerformance lastGrade = null;

        for (AcademicPerformance grade : grades) {
            if (grade.getGrade() != null && grade.getAssessmentDate() != null) {
                if (lastGrade == null || grade.getAssessmentDate().isAfter(lastGrade.getAssessmentDate())) {
                    lastGrade = grade;
                }
            }
        }

        return lastGrade != null ? lastGrade.getGrade() : null;
    }

    /**
     * Подсчитывает количество оценок по значениям от 1 до 5.
     *
     * @param grades список оценок
     * @return объект с количеством оценок для каждого значения
     */
    private Map<Integer, Integer> countGradesByValue(List<AcademicPerformance> grades) {
        Map<Integer, Integer> counts = new HashMap<>();

        for (int i = 1; i <= 5; i++) {
            counts.put(i, 0);
        }

        if (grades != null) {
            for (AcademicPerformance grade : grades) {
                if (grade.getGrade() != null && grade.getGrade() >= 2 && grade.getGrade() <= 5) {
                    Integer currentCount = counts.get(grade.getGrade());
                    counts.put(grade.getGrade(), currentCount + 1);
                }
            }
        }

        return counts;
    }

    /**
     * Получает список преподавателей, ведущих указанный предмет.
     *
     * @param subjectId идентификатор предмета
     * @return список имен преподавателей или пустой список если преподаватели не найдены
     * @throws IllegalArgumentException если subjectId равен null
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