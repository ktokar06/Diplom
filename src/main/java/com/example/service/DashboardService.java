package com.example.service;

import com.example.model.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

/**
 * Сервис для работы с данными главной страницы (dashboard) студента.
 * Предоставляет методы для подготовки и расчета данных успеваемости и посещаемости.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GradeService gradeService;
    private final StudentService studentService;
    private final SummaryService summaryService;

    /**
     * Добавляет данные студента для отображения на главной странице.
     * Включает данные об оценках по предметам, имена преподавателей и средний балл.
     *
     * @param model объект модели для добавления атрибутов
     * @param student объект студента
     */
    public void addStudentDataToModel(Model model, Student student) {
        Integer currentSemester = getCurrentSemester();
        addStudentDataToModel(model, student, currentSemester);
    }

    /**
     * Добавляет данные студента для отображения на главной странице с указанием семестра.
     *
     * @param model объект модели для добавления атрибутов
     * @param student объект студента
     * @param semester семестр для фильтрации данных
     */
    public void addStudentDataToModel(Model model, Student student, Integer semester) {
        Map<Long, Map<String, Object>> subjectsData = gradeService.getGradesDashboard(student.getId(), semester);
        Map<Long, String> teacherNames = studentService.buildTeacherNames(subjectsData.keySet());

        Double overallAverageGrade = calculateOverallAverageGrade(subjectsData.values());

        model.addAttribute("subjects", subjectsData.values());
        model.addAttribute("teacherNames", teacherNames);
        model.addAttribute("group", student.getGroup());
        model.addAttribute("overallAverageGrade", overallAverageGrade);
        model.addAttribute("currentSemester", semester);
    }

    /**
     * Добавляет сводные данные об успеваемости и посещаемости в модель.
     *
     * @param model объект модели
     * @param studentId идентификатор студента
     * @param semester семестр для фильтрации
     */
    public void addSummaryDataToModel(Model model, Long studentId, Integer semester) {
        Map<String, Object> summaryData = summaryService.getSummaryData(studentId, semester);
        model.addAttribute("summaryData", summaryData);
        model.addAttribute("currentSemester", semester);
    }

    /**
     * Добавляет сводные данные для текущего семестра в модель.
     *
     * @param model объект модели
     * @param studentId идентификатор студента
     */
    public void addSummaryDataToModel(Model model, Long studentId) {
        Integer currentSemester = getCurrentSemester();
        addSummaryDataToModel(model, studentId, currentSemester);
    }

    /**
     * Вычисляет текущий семестр на основе текущей даты.
     *
     * @return номер текущего семестра (1 или 2)
     */
    private Integer getCurrentSemester() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        return (month >= 2 && month <= 7) ? 2 : 1;
    }

    /**
     * Вычисляет общий средний балл студента по всем предметам.
     * Учитывает только предметы с валидными данными о среднем балле.
     *
     * @param subjectsData коллекция данных по предметам
     * @return общий средний балл или 0.0 если данные отсутствуют
     */
    private Double calculateOverallAverageGrade(Collection<Map<String, Object>> subjectsData) {
        if (subjectsData == null || subjectsData.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        int count = 0;

        for (Map<String, Object> subjectMap : subjectsData) {
            Object avgGradeObj = subjectMap.get("avgGrade");
            if (avgGradeObj instanceof Double) {
                Double avgGrade = (Double) avgGradeObj;
                if (avgGrade != null && avgGrade > 0) {
                    sum += avgGrade;
                    count++;
                }
            }
        }

        return count > 0 ? sum / count : 0.0;
    }
}