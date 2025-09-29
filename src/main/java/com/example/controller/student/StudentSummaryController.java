package com.example.controller.student;

import com.example.model.Student;
import com.example.security.PersonDetails;
import com.example.service.StudentService;
import com.example.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentSummaryController {

    private final SummaryService summaryService;
    private final StudentService studentService;

    @GetMapping("/summary")
    public String summary(Model model, @AuthenticationPrincipal PersonDetails personDetails) {
        if (!studentService.isStudentAuthenticated(personDetails)) {
            return "redirect:/login";
        }

        Student student = personDetails.student();
        studentService.addCommonAttributes(model, student, "summary");

        Object summaryData = summaryService.getSummaryData(student.getId());
        model.addAttribute("summaryData", summaryData);

        return "student/summary/summary";
    }
}