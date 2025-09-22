package com.example.controller;

import com.example.model.entity.Student;
import com.example.model.entity.Subject;
import com.example.repository.StudentRepository;
import com.example.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final StudentRepository studentRepository;

    @GetMapping("/grades")
    public String grades(Model model) {
        Optional<Student> studentOpt = getCurrentStudent();
        if (studentOpt.isEmpty()) {
            return "redirect:/login";
        }

        Student student = studentOpt.get();
        addCommonAttributes(model, student, "grades");

        Map<Long, Map<String, Object>> cards = gradeService.getGradesDashboard(student.getId());
        Map<Long, String> teacherNames = buildTeacherNames(cards.keySet());

        model.addAttribute("cards", cards);
        model.addAttribute("teacherNames", teacherNames);

        return "student/grade/grades";
    }

    @GetMapping("/grades/{subjectId}")
    public String gradesDetail(@PathVariable Long subjectId, Model model) {
        Optional<Student> studentOpt = getCurrentStudent();
        if (studentOpt.isEmpty()) {
            return "redirect:/grades";
        }

        Student student = studentOpt.get();
        addCommonAttributes(model, student, "grades");

        Map<String, Object> details = gradeService.getSubjectDetails(subjectId, student.getId());
        if (details == null) {
            return "redirect:/grades";
        }

        model.addAttribute("subject", details.get("subject"));
        model.addAttribute("grades", details.get("grades"));
        model.addAttribute("avgGrade", details.get("avgGrade"));
        model.addAttribute("maxGrade", details.get("maxGrade"));
        model.addAttribute("minGrade", details.get("minGrade"));
        model.addAttribute("lastGrade", details.get("lastGrade"));
        model.addAttribute("gradeCounts", details.get("gradeCounts"));
        model.addAttribute("totalGrades", details.get("totalGrades"));

        Object subjectObj = details.get("subject");
        if (subjectObj instanceof Subject subject) {
            List<String> teachers = gradeService.getTeacherNamesBySubjectId(subject.getSubjectId());
            model.addAttribute("teacherName", String.join(", ", teachers));
        } else {
            model.addAttribute("teacherName", "Не назначен");
        }

        return "student/grade/grades-detail";
    }

    private Optional<Student> getCurrentStudent() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return Optional.empty();
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null) {
            return Optional.empty();
        }

        return studentRepository.findByStudentTicketNumber(username);
    }

    private void addCommonAttributes(Model model, Student student, String activePage) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            model.addAttribute("fullName", student.getFullName());
            if (SecurityContextHolder.getContext().getAuthentication().getAuthorities() != null &&
                    !SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty()) {
                model.addAttribute("role", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
            } else {
                model.addAttribute("role", "UNKNOWN");
            }
            model.addAttribute("activePage", activePage);
        }
    }

    private Map<Long, String> buildTeacherNames(Iterable<Long> subjectIds) {
        Map<Long, String> teacherNames = new HashMap<>();

        for (Long subjectId : subjectIds) {
            List<String> teachers = gradeService.getTeacherNamesBySubjectId(subjectId);
            teacherNames.put(subjectId, String.join(", ", teachers));
        }

        return teacherNames;
    }
}