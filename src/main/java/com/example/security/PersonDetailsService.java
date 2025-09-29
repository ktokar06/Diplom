package com.example.security;

import com.example.model.Student;
import com.example.repository.StudentRepository;
import com.example.util.PersonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис для загрузки данных пользователя для аутентификации Spring Security.
 * Реализует интерфейс UserDetailsService для интеграции с Spring Security.
 */
@Service
@RequiredArgsConstructor
public class PersonDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final PersonValidator personValidator;

    /**
     * Загружает данные пользователя по номеру студенческого билета.
     * Используется Spring Security для аутентификации пользователя.
     * Выполняет поиск студента в базе данных и валидацию его данных.
     *
     * @param username номер студенческого билета (используется как имя пользователя)
     * @return объект UserDetails с данными пользователя
     * @throws UsernameNotFoundException если пользователь с указанным номером билета не найден
     *                                   или данные пользователя не прошли валидацию
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Student> student = studentRepository.findByStudentTicketNumber(username);

        if (student.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        if (!personValidator.validateStudent(student.get())) {
            throw new UsernameNotFoundException("Невалидные данные пользователя: " + username);
        }

        return new PersonDetails(student.get());
    }
}