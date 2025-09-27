package com.example.controller;

import com.example.model.entity.Group;
import com.example.model.entity.Student;
import com.example.repository.GroupRepository;
import com.example.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/student/dashboard";
    }

    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout) {

        List<Group> groups = groupRepository.findAll();
        Map<Long, List<Student>> studentsMap = new HashMap<>();

        for (Group group : groups) {
            List<Student> students = studentRepository.findByGroupId(group.getId());
            studentsMap.put(group.getId(), students);
        }

        model.addAttribute("groups", groups);
        model.addAttribute("studentsMap", studentsMap);

        if ("true".equals(error)) {
            model.addAttribute("errorMessage", "Неверные учетные данные");
        }
        if ("true".equals(logout)) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }

        return "login";
    }
}