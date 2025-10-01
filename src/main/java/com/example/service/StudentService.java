package com.example.service;

import com.example.model.Student;
import com.example.model.AcademicPerformance;
import com.example.model.Attendance;
import com.example.repository.StudentRepository;
import com.example.repository.AcademicPerformanceRepository;
import com.example.repository.AttendanceRepository;
import com.example.security.PersonDetails;
import com.example.util.PersonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.*;

/**
 * Сервис для работы с данными студентов.
 * Предоставляет методы для аутентификации, управления данными и построения интерфейсов.
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final GradeService gradeService;
    private final StudentRepository studentRepository;
    private final PersonValidator personValidator;
    private final AcademicPerformanceRepository academicPerformanceRepository;
    private final AttendanceRepository attendanceRepository;

    /**
     * Проверяет, аутентифицирован ли студент в системе на основе данных пользователя.
     * Использует PersonValidator для проверки валидности данных студента.
     *
     * @param personDetails детали пользователя для проверки аутентификации
     * @return true если студент аутентифицирован и его данные валидны, false в противном случае
     */
    public boolean isStudentAuthenticated(PersonDetails personDetails) {
        if (personDetails == null) {
            return false;
        }

        Student student = personDetails.student();
        if (student == null) {
            return false;
        }

        return personValidator.validateStudent(student);
    }

    /**
     * Добавляет общие атрибуты для всех студенческих страниц в модель.
     * Включает полное имя студента, роль и активную страницу для навигации.
     *
     * @param model объект модели для добавления атрибутов
     * @param student объект студента
     * @param activePage название активной страницы для выделения в навигации
     */
    public void addCommonAttributes(Model model, Student student, String activePage) {
        model.addAttribute("fullName", student.getFullName());
        model.addAttribute("role", "ROLE_STUDENT");
        model.addAttribute("activePage", activePage);
    }

    /**
     * Создает структуру соответствия идентификаторов предметов и имен преподавателей.
     * Для каждого предмета получает список преподавателей и форматирует их в строку.
     *
     * @param subjectIds итерируемая коллекция идентификаторов предметов
     * @return структура где ключ - идентификатор предмета, значение - строка с именами преподавателей
     */
    public Map<Long, String> buildTeacherNames(Iterable<Long> subjectIds) {
        Map<Long, String> teacherNames = new HashMap<>();

        for (Long subjectId : subjectIds) {
            List<String> teachers = gradeService.getTeacherNamesBySubjectId(subjectId);
            String teacherNameString = buildTeacherNameString(teachers);
            teacherNames.put(subjectId, teacherNameString);
        }

        return teacherNames;
    }

    /**
     * Объединяет список имен преподавателей в одну строку через запятую.
     * Обрабатывает случаи пустого списка или null значений.
     *
     * @param teachers список имен преподавателей
     * @return строка с объединенными именами преподавателей или пустая строка если список пуст
     */
    public String buildTeacherNameString(List<String> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            return "";
        }

        StringBuilder teacherNameBuilder = new StringBuilder();
        for (int i = 0; i < teachers.size(); i++) {
            teacherNameBuilder.append(teachers.get(i));
            if (i < teachers.size() - 1) {
                teacherNameBuilder.append(", ");
            }
        }
        return teacherNameBuilder.toString();
    }

    /**
     * Обновляет пароль студента в системе.
     * Выполняет поиск студента по ID и обновляет хэш пароля.
     *
     * @param studentId идентификатор студента
     * @param newPasswordHash новый хэш пароля (уже закодированный)
     * @throws RuntimeException если студент с указанным ID не найден
     */
    public void updatePassword(Long studentId, String newPasswordHash) {
        Optional<Student> studentOptional = studentRepository.findById(studentId);
        if (!studentOptional.isPresent()) {
            throw new RuntimeException();
        }

        Student student = studentOptional.get();
        student.setPasswordHash(newPasswordHash);
        studentRepository.save(student);
    }

    /**
     * Получает список доступных семестров для студента на основе имеющихся данных об оценках и посещаемости.
     *
     * @param studentId идентификатор студента
     * @return список доступных семестров, отсортированный по возрастанию
     */
    public List<Integer> getAvailableSemesters(Long studentId) {
        Set<Integer> semesters = new HashSet<>();

        List<AcademicPerformance> grades = academicPerformanceRepository.findByStudentId(studentId);
        for (AcademicPerformance grade : grades) {
            if (grade.getSubject() != null && grade.getSubject().getSemester() != null) {
                semesters.add(grade.getSubject().getSemester());
            }
        }

        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        for (Attendance attendance : attendances) {
            if (attendance.getSubject() != null && attendance.getSubject().getSemester() != null) {
                semesters.add(attendance.getSubject().getSemester());
            }
        }

        List<Integer> sortedSemesters = new ArrayList<>(semesters);
        Collections.sort(sortedSemesters);

        return sortedSemesters.isEmpty() ? Arrays.asList(1, 2) : sortedSemesters;
    }

    /**
     * Получает текущий семестр на основе текущей даты.
     *
     * @return номер текущего семестра (1 или 2)
     */
    public int getCurrentSemester() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        return (month >= 2 && month <= 7) ? 2 : 1;
    }
}