package com.example.controller;

import com.example.model.entity.Student;
import com.example.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ChangePasswordController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароли не совпадают!");
            return "redirect:/dashboard";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Пароль должен быть не менее 6 символов!");
            return "redirect:/dashboard";
        }

        Student student = getCurrentStudent();
        if (student != null) {
            student.setPasswordHash(passwordEncoder.encode(newPassword));
            studentRepository.save(student);
            redirectAttributes.addFlashAttribute("successMessage", "Пароль успешно изменён!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при изменении пароля!");
        }

        return "redirect:/dashboard";
    }

    private Student getCurrentStudent() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return studentRepository.findByStudentTicketNumber(auth.getName()).orElse(null);
    }
}
