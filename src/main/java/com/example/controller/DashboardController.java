package com.example.controller;

import com.example.model.entity.Student;
import com.example.model.entity.Teacher;
import com.example.repository.StudentRepository;
import com.example.repository.TeacherRepository;
import com.example.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final GradeService gradeService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "success", required = false) String success) {

        if ("passwordMismatch".equals(error)) {
            model.addAttribute("errorMessage", "Пароли не совпадают!");
        } else if ("passwordTooShort".equals(error)) {
            model.addAttribute("errorMessage", "Пароль должен быть не менее 6 символов!");
        }

        if ("passwordChanged".equals(success)) {
            model.addAttribute("successMessage", "Пароль успешно изменён!");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String role = auth.getAuthorities().iterator().next().getAuthority();
        String username = auth.getName();
        String fullName = determineFullName(username, role);

        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("fullName", fullName);
        model.addAttribute("activePage", "dashboard");

        if ("ROLE_STUDENT".equals(role)) {
            Student student = studentRepository.findByStudentTicketNumber(username).orElse(null);

            if (student != null) {
                addStudentDataToModel(model, student);
            }

            return "student/dashboard";
        }

        return "student/dashboard";
    }

    private String determineFullName(String username, String role) {
        if ("ROLE_STUDENT".equals(role)) {
            return studentRepository.findByStudentTicketNumber(username)
                    .map(Student::getFullName)
                    .orElse(username);
        } else if ("ROLE_TEACHER".equals(role)) {
            return teacherRepository.findByFullName(username)
                    .map(Teacher::getFullName)
                    .orElse(username);
        }

        return username;
    }

    private void addStudentDataToModel(Model model, Student student) {
        Map<Long, Map<String, Object>> subjectsData = gradeService.getGradesDashboard(student.getId());
        Map<Long, String> teacherNames = new HashMap<>();

        for (Long subjectId : subjectsData.keySet()) {
            String names = String.join(", ", gradeService.getTeacherNamesBySubjectId(subjectId));
            teacherNames.put(subjectId, names);
        }

        model.addAttribute("subjects", subjectsData.values());
        model.addAttribute("teacherNames", teacherNames);
        model.addAttribute("group", student.getGroup());
    }
}
