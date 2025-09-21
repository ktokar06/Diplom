package com.example.controller;

import com.example.model.entity.Student;
import com.example.repository.StudentRepository;
import com.example.service.AttendanceService;
import com.example.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final GradeService gradeService;
    private final StudentRepository studentRepository;

    @GetMapping("/attendance")
    public String attendance(Model model) {
        Student student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        addCommonAttributes(model, student, "attendance");
        Map<Long, Map<String, Object>> cards = attendanceService.getAttendanceDashboard(student.getId());
        Map<Long, String> teacherNames = buildTeacherNames(cards.keySet());

        model.addAttribute("cards", cards);
        model.addAttribute("teacherNames", teacherNames);
        return "student/attendance/attendance"; // Исправленный путь
    }

    @GetMapping("/attendance/{subjectId}")
    public String attendanceDetail(@PathVariable Long subjectId, Model model) {
        Student student = getCurrentStudent();
        if (student == null) {
            return "redirect:/login";
        }

        addCommonAttributes(model, student, "attendance");
        Map<String, Object> details = attendanceService.getAttendanceDetails(subjectId, student.getId());

        if (details == null) {
            return "redirect:/attendance";
        }

        Map<Long, String> teacherNames = new HashMap<>();
        String teacherNameString = buildTeacherNameString(subjectId);
        teacherNames.put(subjectId, teacherNameString);

        model.addAllAttributes(details);
        model.addAttribute("teacherNames", teacherNames);
        return "student/attendance/attendance-detail";
    }

    private Student getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return studentRepository.findByStudentTicketNumber(auth.getName()).orElse(null);
    }

    private void addCommonAttributes(Model model, Student student, String activePage) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (student != null) {
            model.addAttribute("fullName", student.getFullName());
        }
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            model.addAttribute("role", auth.getAuthorities().iterator().next().getAuthority());
        } else {
            model.addAttribute("role", "UNKNOWN");
        }
        model.addAttribute("activePage", activePage);
    }

    private Map<Long, String> buildTeacherNames(Iterable<Long> subjectIds) {
        Map<Long, String> teacherNames = new HashMap<>();
        for (Long subjectId : subjectIds) {
            String namesString = buildTeacherNameString(subjectId);
            teacherNames.put(subjectId, namesString);
        }
        return teacherNames;
    }

    private String buildTeacherNameString(Long subjectId) {
        var names = gradeService.getTeacherNamesBySubjectId(subjectId);
        return String.join(", ", names);
    }
}