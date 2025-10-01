package com.example.controller.student;

import com.example.model.Student;
import com.example.security.PersonDetails;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentGradeController {

    private final GradeService gradeService;
    private final StudentService studentService;

    @GetMapping("/grades")
    public String grades(@RequestParam(value = "semester", required = false) Integer semester,
                         Model model, @AuthenticationPrincipal PersonDetails personDetails) {
        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "grades");

        if (semester == null) {
            semester = studentService.getCurrentSemester();
        }

        Map<Long, Map<String, Object>> cards = gradeService.getGradesDashboard(student.getId(), semester);
        Map<Long, String> teacherNames = studentService.buildTeacherNames(cards.keySet());

        model.addAttribute("cards", cards);
        model.addAttribute("teacherNames", teacherNames);
        model.addAttribute("currentSemester", semester);
        model.addAttribute("availableSemesters", studentService.getAvailableSemesters(student.getId()));

        return "student/grade/grades";
    }

    @GetMapping("/grades/{subjectId}")
    public String gradesDetail(@PathVariable Long subjectId,
                               @RequestParam(value = "semester", required = false) Integer semester,
                               Model model, @AuthenticationPrincipal PersonDetails personDetails) {
        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "grades");

        if (semester == null) {
            semester = studentService.getCurrentSemester();
        }

        Map<String, Object> details = gradeService.getSubjectDetails(subjectId, student.getId(), semester);
        if (details == null) {
            return "redirect:/student/grades";
        }

        gradeService.addGradeDetailsToModel(model, details, subjectId);
        model.addAttribute("currentSemester", semester);
        model.addAttribute("availableSemesters", studentService.getAvailableSemesters(student.getId()));

        return "student/grade/grades-detail";
    }
}