package com.example.controller.student;

import com.example.model.Student;
import com.example.security.PersonDetails;
import com.example.service.AttendanceService;
import com.example.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;
    private final StudentService studentService;

    @GetMapping("/attendance")
    public String attendance(@RequestParam(value = "semester", required = false) Integer semester,
                             Model model, @AuthenticationPrincipal PersonDetails personDetails) {
        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "attendance");

        if (semester == null) {
            semester = studentService.getCurrentSemester();
        }

        Map<Long, Map<String, Object>> cards = attendanceService.getAttendanceDashboard(student.getId(), semester);
        Map<Long, String> teacherNames = studentService.buildTeacherNames(cards.keySet());

        model.addAttribute("cards", cards);
        model.addAttribute("teacherNames", teacherNames);
        model.addAttribute("currentSemester", semester);
        model.addAttribute("availableSemesters", studentService.getAvailableSemesters(student.getId()));

        return "student/attendance/attendance";
    }

    @GetMapping("/attendance/{subjectId}")
    public String attendanceDetail(@PathVariable Long subjectId,
                                   @RequestParam(value = "semester", required = false) Integer semester,
                                   Model model, @AuthenticationPrincipal PersonDetails personDetails) {
        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "attendance");

        if (semester == null) {
            semester = studentService.getCurrentSemester();
        }

        Map<String, Object> details = attendanceService.getAttendanceDetails(subjectId, student.getId(), semester);
        if (details == null) {
            return "redirect:/student/attendance";
        }

        Set<Long> subjectIds = Collections.singleton(subjectId);
        Map<Long, String> teacherNames = studentService.buildTeacherNames(subjectIds);

        model.addAttribute("teacherNames", teacherNames);
        model.addAttribute("currentSemester", semester);
        model.addAttribute("availableSemesters", studentService.getAvailableSemesters(student.getId()));
        model.addAllAttributes(details);

        return "student/attendance/attendance-detail";
    }
}