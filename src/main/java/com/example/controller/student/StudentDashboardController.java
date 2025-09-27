package com.example.controller.student;

import com.example.model.entity.Student;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentDashboardController {

    private final StudentService studentService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @AuthenticationPrincipal PersonDetails personDetails,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "success", required = false) String success) {

        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        if ("passwordMismatch".equals(error)) {
            model.addAttribute("errorMessage", "Пароли не совпадают!");
        } else if ("passwordTooShort".equals(error)) {
            model.addAttribute("errorMessage", "Пароль должен быть не менее 4 символов!");
        }
        if ("passwordChanged".equals(success)) {
            model.addAttribute("successMessage", "Пароль успешно изменён!");
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "dashboard");
        dashboardService.addStudentDataToModel(model, student);

        return "student/dashboard";
    }
}