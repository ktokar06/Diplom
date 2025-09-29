package com.example.controller.student;

import com.example.model.Student;
import com.example.security.PersonDetails;
import com.example.service.StudentService;
import com.example.util.PersonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentChangePasswordController {

    private final PasswordEncoder passwordEncoder;
    private final StudentService studentService;
    private final PersonValidator personValidator;

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal PersonDetails personDetails,
            RedirectAttributes redirectAttributes) {

        if (personDetails == null || personDetails.student() == null) {
            return "redirect:/login";
        }

        if (!personValidator.validatePassword(newPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароль должен быть не менее 2 символов!");
            return "redirect:/student/dashboard";
        }

        if (!personValidator.validatePasswordsMatch(newPassword, confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароли не совпадают!");
            return "redirect:/student/dashboard";
        }

        try {
            Student student = personDetails.student();
            String encodedPassword = passwordEncoder.encode(newPassword);
            studentService.updatePassword(student.getId(), encodedPassword);

            redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно изменён!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при изменении пароля: " + e.getMessage());
        }

        return "redirect:/student/dashboard";
    }
}