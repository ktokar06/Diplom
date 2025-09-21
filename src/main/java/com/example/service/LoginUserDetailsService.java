package com.example.service;

import com.example.model.entity.Student;
import com.example.model.entity.Teacher;
import com.example.repository.StudentRepository;
import com.example.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Student> student = studentRepository.findByStudentTicketNumber(username);
        if (student.isPresent()) {
            return User.builder()
                    .username(student.get().getStudentTicketNumber())
                    .password(student.get().getPasswordHash())
                    .roles("STUDENT")
                    .build();
        }
        Optional<Teacher> teacher = teacherRepository.findByFullName(username);
        if (teacher.isPresent()) {
            return User.builder()
                    .username(teacher.get().getFullName())
                    .password(teacher.get().getPasswordHash())
                    .roles("TEACHER")
                    .build();
        }
        throw new UsernameNotFoundException("Пользователь не найден: " + username);
    }
}