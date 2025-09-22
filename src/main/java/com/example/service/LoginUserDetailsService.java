package com.example.service;

import com.example.model.entity.Student;
import com.example.repository.StudentRepository;
import com.example.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис для аутентификации пользователей.
 */
@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    /**
     * Загружает пользователя по имени (логину).
     *
     * @param username имя пользователя.
     * @return объект UserDetails.
     * @throws UsernameNotFoundException если пользователь не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Student> student = studentRepository.findByStudentTicketNumber(username);
        if (student.isPresent()) {
            Student s = student.get();
            return User.builder()
                    .username(s.getStudentTicketNumber())
                    .password(s.getPasswordHash())
                    .roles("STUDENT")
                    .build();
        }

        throw new UsernameNotFoundException("Пользователь не найден: " + username);
    }
}