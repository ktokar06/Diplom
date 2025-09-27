package com.example.controller.student;

import com.example.model.entity.Student;
import com.example.security.PersonDetails;
import com.example.service.GradeDetailService;
import com.example.service.GradeService;
import com.example.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentGradeController {

    private final GradeService gradeService;
    private final StudentService studentService;
    private final GradeDetailService gradeDetailService;

    @GetMapping("/grades")
    public String grades(Model model, @AuthenticationPrincipal PersonDetails personDetails) {
        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "grades");

        Map<Long, Map<String, Object>> cards = gradeService.getGradesDashboard(student.getId());
        Map<Long, String> teacherNames = studentService.buildTeacherNames(cards.keySet());

        model.addAttribute("cards", cards);
        model.addAttribute("teacherNames", teacherNames);
        return "student/grade/grades";
    }

    @GetMapping("/grades/{subjectId}")
    public String gradesDetail(@PathVariable Long subjectId, Model model,
                               @AuthenticationPrincipal PersonDetails personDetails) {
        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "grades");

        Map<String, Object> details = gradeService.getSubjectDetails(subjectId, student.getId());
        if (details == null) {
            return "redirect:/student/grades";
        }

        gradeDetailService.addGradeDetailsToModel(model, details, subjectId);
        return "student/grade/grades-detail";
    }
}