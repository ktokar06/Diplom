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

import java.util.*;

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

        addCommonAttributes(model, student);
        Map<Long, Map<String, Object>> cards = attendanceService.getAttendanceDashboard(student.getId());
        Map<Long, String> teacherNames = buildTeacherNames(cards.keySet());

        model.addAttribute("cards", cards);
        model.addAttribute("teacherNames", teacherNames);
        return "student/attendance/attendance";
    }

    @GetMapping("/attendance/{subjectId}")
    public String attendanceDetail(@PathVariable Long subjectId, Model model) {
        Student student = getCurrentStudent();

        if (student == null) {
            return "redirect:/login";
        }

        addCommonAttributes(model, student);
        Map<String, Object> details = attendanceService.getAttendanceDetails(subjectId, student.getId());

        if (details == null) {
            return "redirect:/attendance";
        }

        String teacherName = String.join(", ", gradeService.getTeacherNamesBySubjectId(subjectId));
        Map<Long, String> teacherNames = new HashMap<>();
        teacherNames.put(subjectId, teacherName);

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

    private void addCommonAttributes(Model model, Student student) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            model.addAttribute("fullName", student.getFullName());
            String role = auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()
                    ? auth.getAuthorities().iterator().next().getAuthority()
                    : "UNKNOWN";
            model.addAttribute("role", role);
            model.addAttribute("activePage", "attendance");
        }
    }

    private Map<Long, String> buildTeacherNames(Iterable<Long> subjectIds) {
        Map<Long, String> teacherNames = new HashMap<>();

        for (Long subjectId : subjectIds) {
            String names = String.join(", ", gradeService.getTeacherNamesBySubjectId(subjectId));
            teacherNames.put(subjectId, names);
        }

        return teacherNames;
    }
}