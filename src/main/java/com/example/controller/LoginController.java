package com.example.controller;

import com.example.model.entity.Group;
import com.example.model.entity.Student;
import com.example.model.entity.Teacher;
import com.example.repository.GroupRepository;
import com.example.repository.StudentRepository;
import com.example.repository.TeacherRepository;
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
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout) {

        List<Group> groups = groupRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        Map<Long, List<Student>> studentsMap = new HashMap<>();

        for (Group group : groups) {
            studentsMap.put(group.getId(), studentRepository.findByGroupId(group.getId()));
        }

        model.addAttribute("groups", groups);
        model.addAttribute("teachers", teachers);
        model.addAttribute("studentsMap", studentsMap);

        if (error != null) {
            model.addAttribute("errorMessage", "Неверные учетные данные");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }

        return "login";
    }
}