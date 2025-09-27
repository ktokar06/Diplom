package com.example.service;

import com.example.model.entity.AcademicPerformance;
import com.example.model.entity.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с детализированными данными оценок студента.
 * Предоставляет методы для подготовки статистики и детальной информации по предметам.
 */
@Service
@RequiredArgsConstructor
public class GradeDetailService {

    private final GradeService gradeService;
    private final StudentService studentService;

    /**
     * Добавляет детализированные данные оценок в модель для отображения.
     * Включает основную информацию о предмете, оценки и статистику.
     *
     * @param model объект модели для добавления атрибутов
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
            List<String> teachers = gradeService.getTeacherNamesBySubjectId(subject.getSubjectId());
            model.addAttribute("teacherName", studentService.buildTeacherNameString(teachers));
        } else {
            model.addAttribute("teacherName", "Не назначен");
        }

        addGradeStatistics(model, details);
    }

    /**
     * Добавляет статистику оценок в модель.
     * Рассчитывает количество оценок по категориям: отлично, хорошо, удовлетворительно, неудовлетворительно.
     *
     * @param model объект модели для добавления атрибутов
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