package com.example.util;

import com.example.model.entity.Student;
import org.springframework.stereotype.Component;

/**
 * Компонент для валидации данных студентов и паролей.
 * Предоставляет методы проверки корректности данных для авторизации и смены пароля.
 */
@Component
public class PersonValidator {

    /**
     * Проверяет валидность объекта студента.
     * Выполняет проверку на null и наличие обязательных полей.
     * Обязательные поля: полное имя, номер студенческого билета и хеш пароля.
     *
     * @param student объект студента для проверки
     * @return true если студент валиден, false если объект null или обязательные поля пустые
     */
    public boolean validateStudent(Student student) {
        if (student == null) {
            return false;
        }

        boolean nameValid = student.getFullName() != null && !student.getFullName().trim().isEmpty();
        boolean ticketValid = student.getStudentTicketNumber() != null && !student.getStudentTicketNumber().trim().isEmpty();
        boolean passwordValid = student.getPasswordHash() != null && !student.getPasswordHash().trim().isEmpty();

        return nameValid && ticketValid && passwordValid;
    }

    /**
     * Проверяет валидность пароля по длине.
     * Минимальная допустимая длина пароля - 4 символа.
     *
     * @param password пароль для проверки
     * @return true если пароль не null и его длина >= 4 символов, иначе false
     */
    public boolean validatePassword(String password) {
        return password != null && password.length() >= 4;
    }

    /**
     * Проверяет совпадение нового пароля и подтверждения пароля.
     * Оба пароля должны быть не null и идентичны.
     *
     * @param newPassword новый пароль
     * @param confirmPassword подтверждение пароля
     * @return true если пароли совпадают и не null, иначе false
     */
    public boolean validatePasswordsMatch(String newPassword, String confirmPassword) {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}