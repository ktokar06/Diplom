package com.example.service;

import com.example.model.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.Map;

/**
 * Сервис для работы с данными главной страницы (dashboard) студента.
 * Предоставляет методы для подготовки и расчета данных успеваемости.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GradeService gradeService;
    private final StudentService studentService;

    /**
     * Добавляет данные студента для отображения на главной странице.
     * Включает данные об оценках по предметам, имена преподавателей и средний балл.
     *
     * @param model объект модели для добавления атрибутов
     * @param student объект студента
     */
    public void addStudentDataToModel(Model model, Student student) {
        Map<Long, Map<String, Object>> subjectsData = gradeService.getGradesDashboard(student.getId());
        Map<Long, String> teacherNames = studentService.buildTeacherNames(subjectsData.keySet());

        Double overallAverageGrade = calculateOverallAverageGrade(subjectsData.values());

        model.addAttribute("subjects", subjectsData.values());
        model.addAttribute("teacherNames", teacherNames);
        model.addAttribute("group", student.getGroup());
        model.addAttribute("overallAverageGrade", overallAverageGrade);
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