package com.example.service;

import com.example.model.AcademicPerformance;
import com.example.model.Subject;
import com.example.model.Teacher;
import com.example.model.TeacherSubject;
import com.example.repository.AcademicPerformanceRepository;
import com.example.repository.SubjectRepository;
import com.example.repository.TeacherSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.*;

/**
 * Сервис для работы с академической успеваемостью студентов.
 * Предоставляет методы для расчета статистики по оценкам, построения дашбордов и детализированных отчетов.
 */
@Service
@RequiredArgsConstructor
public class GradeService {

    private final AcademicPerformanceRepository academicPerformanceRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    private static final Map<String, Double> WORK_TYPE_WEIGHTS = new HashMap<>();

    static {
        WORK_TYPE_WEIGHTS.put("Экзамен", 3.0);
        WORK_TYPE_WEIGHTS.put("Контрольная работа", 2.0);
        WORK_TYPE_WEIGHTS.put("Урок", 1.0);
        WORK_TYPE_WEIGHTS.put("Домашнее задание", 1.0);
    }

    /**
     * Получает дашборд с оценками студента по всем предметам с возможностью фильтрации по семестру.
     *
     * @param studentId идентификатор студента
     * @param semester семестр для фильтрации (может быть null)
     * @return структура данных где ключ - ID предмета, значение - данные по предмету
     * @throws IllegalArgumentException если studentId равен null
     */
    public Map<Long, Map<String, Object>> getGradesDashboard(Long studentId, Integer semester) {
        if (studentId == null) {
            throw new IllegalArgumentException();
        }

        List<AcademicPerformance> performances = academicPerformanceRepository.findByStudentId(studentId);

        if (semester != null) {
            List<AcademicPerformance> filteredPerformances = new ArrayList<>();
            for (AcademicPerformance performance : performances) {
                if (performance.getSubject() != null && Objects.equals(semester, performance.getSubject().getSemester())) {
                    filteredPerformances.add(performance);
                }
            }
            performances = filteredPerformances;
        }

        Map<Long, List<AcademicPerformance>> groupedBySubject = groupPerformancesBySubject(performances);
        return buildDashboardData(groupedBySubject);
    }

    /**
     * Группирует оценки по предметам.
     *
     * @param performances список оценок
     * @return структура сгруппированных оценок по ID предмета
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
     * Строит данные дашборда на основе сгруппированных оценок.
     *
     * @param groupedBySubject сгруппированные оценки по предметам
     * @return структура данных дашборда
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
     * Создает данные по предмету для дашборда.
     *
     * @param subject предмет
     * @param grades список оценок по предмету
     * @return структура с данными предмета
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
     * Получает детализированную информацию по оценкам для конкретного предмета.
     *
     * @param subjectId идентификатор предмета
     * @param studentId идентификатор студента
     * @param semester семестр для фильтрации
     * @return структура с детализированными данными или null если предмет не найден
     * @throws IllegalArgumentException если subjectId или studentId равны null
     */
    public Map<String, Object> getSubjectDetails(Long subjectId, Long studentId, Integer semester) {
        if (subjectId == null || studentId == null) {
            throw new IllegalArgumentException();
        }

        Optional<Subject> subjectOpt = subjectRepository.findById(subjectId);

        if (subjectOpt.isEmpty()) {
            return null;
        }

        Subject subject = subjectOpt.get();
        List<AcademicPerformance> grades = academicPerformanceRepository
                .findByStudentIdAndSubjectSubjectId(studentId, subjectId);

        if (semester != null && !Objects.equals(semester, subject.getSemester())) {
            grades = Collections.emptyList();
        }

        if (grades == null) {
            grades = Collections.emptyList();
        }

        return createSubjectDetails(subject, grades);
    }

    /**
     * Создает детализированные данные по предмету.
     *
     * @param subject предмет
     * @param grades список оценок
     * @return структура с детализированными данными
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
     * Вычисляет средневзвешенный балл по оценкам с учетом весов типов работ.
     *
     * @param grades список оценок
     * @return средневзвешенный балл или null если оценки отсутствуют
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
     * Находит максимальную оценку в списке.
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
     * Находит минимальную оценку в списке.
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
     * Получает последнюю оценку по дате оценки.
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
     * @return структура с количеством оценок по значениям
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
     * Получает список преподавателей по идентификатору предмета.
     *
     * @param subjectId идентификатор предмета
     * @return список имен преподавателей
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

    /**
     * Форматирует список преподавателей в строку.
     *
     * @param teachers список имен преподавателей
     * @return строка с именами преподавателей через запятую
     */
    public String buildTeacherNameString(List<String> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            return "Не назначен";
        }

        StringBuilder teacherNameBuilder = new StringBuilder();
        for (int i = 0; i < teachers.size(); i++) {
            teacherNameBuilder.append(teachers.get(i));
            if (i < teachers.size() - 1) {
                teacherNameBuilder.append(", ");
            }
        }
        return teacherNameBuilder.toString();
    }

    /**
     * Добавляет детали оценок в модель для отображения в представлении.
     *
     * @param model объект модели
     * @param details детализированные данные по предмету
     * @param subjectId идентификатор предмета
     */
    public void addGradeDetailsToModel(Model model, Map<String, Object> details, Long subjectId) {
        model.addAttribute("subject", details.get("subject"));
        model.addAttribute("grades", details.get("grades"));
        model.addAttribute("avgGrade", details.get("avgGrade"));
        model.addAttribute("maxGrade", details.get("maxGrade"));
        model.addAttribute("minGrade", details.get("minGrade"));
        model.addAttribute("lastGrade", details.get("lastGrade"));

        Object subjectObj = details.get("subject");
        if (subjectObj instanceof Subject) {
            Subject subject = (Subject) subjectObj;
            List<String> teachers = getTeacherNamesBySubjectId(subject.getSubjectId());
            model.addAttribute("teacherName", buildTeacherNameString(teachers));
        } else {
            model.addAttribute("teacherName", "Не назначен");
        }

        addGradeStatistics(model, details);
    }

    /**
     * Добавляет статистику по оценкам в модель.
     *
     * @param model объект модели
     * @param details детализированные данные по предмету
     */
    private void addGradeStatistics(Model model, Map<String, Object> details) {
        List<AcademicPerformance> gradeList = (List<AcademicPerformance>) details.get("grades");

        long excellentCount = 0;
        long goodCount = 0;
        long satisfactoryCount = 0;
        long unsatisfactoryCount = 0;

        if (gradeList != null) {
            for (AcademicPerformance grade : gradeList) {
                if (grade.getGrade() != null) {
                    Double gradeValue = Double.valueOf(grade.getGrade());
                    if (gradeValue >= 4.5) {
                        excellentCount++;
                    } else if (gradeValue >= 3.5) {
                        goodCount++;
                    } else if (gradeValue >= 2.5) {
                        satisfactoryCount++;
                    } else {
                        unsatisfactoryCount++;
                    }
                }
            }
        }

        model.addAttribute("excellentCount", excellentCount);
        model.addAttribute("goodCount", goodCount);
        model.addAttribute("satisfactoryCount", satisfactoryCount);
        model.addAttribute("unsatisfactoryCount", unsatisfactoryCount);
    }
}