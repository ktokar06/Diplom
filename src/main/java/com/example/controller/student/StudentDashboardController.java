package com.example.controller.student;

import com.example.model.Student;
import com.example.security.PersonDetails;
import com.example.service.DashboardService;
import com.example.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentDashboardController {

    private final StudentService studentService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal PersonDetails personDetails) {
        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "dashboard");
        dashboardService.addStudentDataToModel(model, student);

        return "student/dashboard";
    }
}