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

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final GradeService gradeService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "redirect:/login";
        }

        String role = auth.getAuthorities().iterator().next().getAuthority();
        String username = auth.getName();
        String fullName = determineFullName(username, role);

        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("fullName", fullName);
        model.addAttribute("activePage", "dashboard");

        if ("STUDENT".equals(role)) {
            var studentOpt = studentRepository.findByStudentTicketNumber(username);
            if (studentOpt.isPresent()) {
                var student = studentOpt.get();
                addStudentDataToModel(model, student);
            }
            return "student/dashboard";
        }

        return "student/dashboard";
    }

    private String determineFullName(String username, String role) {
        switch (role) {
            case "STUDENT":
                var studentOpt = studentRepository.findByStudentTicketNumber(username);
                return studentOpt.map(Student::getFullName).orElse(username);
            case "TEACHER":
                var teacherOpt = teacherRepository.findByFullName(username);
                return teacherOpt.map(Teacher::getFullName).orElse(username);
            default:
                return username;
        }
    }

    private void addStudentDataToModel(Model model, Student student) {
        Map<Long, Map<String, Object>> subjectsData = gradeService.getGradesDashboard(student.getId());
        Map<Long, String> teacherNames = buildTeacherNames(subjectsData.keySet());
        model.addAttribute("subjects", subjectsData.values());
        model.addAttribute("teacherNames", teacherNames);
    }

    private Map<Long, String> buildTeacherNames(Iterable<Long> subjectIds) {
        Map<Long, String> teacherNames = new HashMap<>();
        for (Long subjectId : subjectIds) {
            var names = gradeService.getTeacherNamesBySubjectId(subjectId);
            teacherNames.put(subjectId, String.join(", ", names));
        }
        return teacherNames;
    }
}