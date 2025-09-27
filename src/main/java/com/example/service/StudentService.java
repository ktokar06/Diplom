package com.example.service;

import com.example.model.entity.Student;
import com.example.security.PersonDetails;
import com.example.util.PersonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для общих операций со студентами.
 * Предоставляет методы для проверки аутентификации, добавления атрибутов в модель
 * и работы с данными преподавателей.
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final PersonValidator personValidator;
    private final GradeService gradeService;

    /**
     * Проверяет, аутентифицирован ли студент в системе.
     * Выполняет проверку наличия объекта PersonDetails и валидности данных студента.
     *
     * @param personDetails детали аутентифицированного пользователя
     * @return true если студент аутентифицирован и валиден, иначе false
     */
    public boolean isStudentAuthenticated(PersonDetails personDetails) {
        if (personDetails == null || personDetails.student() == null) {
            return false;
        }

        return personValidator.validateStudent(personDetails.student());
    }

    /**
     * Добавляет общие атрибуты для всех студенческих страниц.
     * Включает полное имя студента, роль и активную страницу для навигации.
     *
     * @param model объект модели для добавления атрибутов
     * @param student объект студента
     * @param activePage идентификатор активной страницы для подсветки в навигации
     */
    public void addCommonAttributes(Model model, Student student, String activePage) {
        model.addAttribute("fullName", student.getFullName());
        model.addAttribute("role", "ROLE_STUDENT");
        model.addAttribute("activePage", activePage);
    }

    /**
     * Строит соответствие между идентификаторами предметов и именами преподавателей.
     * Для каждого предмета получает список преподавателей и объединяет их в строку.
     *
     * @param subjectIds коллекция идентификаторов предметов
     * @return соответствие ID предмета → строка с именами преподавателей
     */
    public Map<Long, String> buildTeacherNames(Iterable<Long> subjectIds) {
        Map<Long, String> teacherNames = new HashMap<>();

        for (Long subjectId : subjectIds) {
            List<String> teachers = gradeService.getTeacherNamesBySubjectId(subjectId);
            teacherNames.put(subjectId, buildTeacherNameString(teachers));
        }

        return teacherNames;
    }

    /**
     * Объединяет список имен преподавателей в одну строку через запятую.
     * Если список пуст, возвращает пустую строку.
     *
     * @param teachers список имен преподавателей
     * @return строка с объединенными именами преподавателей
     */
    public String buildTeacherNameString(List<String> teachers) {
        StringBuilder teacherNameBuilder = new StringBuilder();
        for (int i = 0; i < teachers.size(); i++) {
            teacherNameBuilder.append(teachers.get(i));
            if (i < teachers.size() - 1) {
                teacherNameBuilder.append(", ");
            }
        }
        return teacherNameBuilder.toString();
    }
}