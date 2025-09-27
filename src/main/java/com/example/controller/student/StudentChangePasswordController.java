package com.example.controller.student;

import com.example.model.entity.Student;
import com.example.security.PersonDetails;
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

    private final PersonValidator personValidator;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal PersonDetails personDetails,
            RedirectAttributes redirectAttributes) {

        if (personDetails == null || personDetails.student() == null ||
                personValidator.validateStudent(personDetails.student())) {
            return "redirect:/login";
        }

        if (!personValidator.validatePasswordsMatch(newPassword, confirmPassword)) {
            redirectAttributes.addAttribute("error", "passwordMismatch");
            return "redirect:/student/dashboard";
        }

        if (!personValidator.validatePassword(newPassword)) {
            redirectAttributes.addAttribute("error", "passwordTooShort");
            return "redirect:/student/dashboard";
        }

        Student student = personDetails.student();
        student.setPasswordHash(passwordEncoder.encode(newPassword));

        redirectAttributes.addAttribute("success", "passwordChanged");
        return "redirect:/student/dashboard";
    }
}