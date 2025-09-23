package com.example.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.service.StudentService.*;

public class StudentTest extends BaseTest {

    private final String full_name = "full_name";
    private final String student_ticket_number = "student_ticket_number";
    private final String password_hash = "password_hash";
    private final String group_name = "group_name";
    private final String role_name = "role_name";

    @Test(description = "Позитивный кейс: Получение существующего студента по ID")
    public void getExistingStudentByIdTest() throws SQLException {
        ResultSet student = getStudentById(1);
        Assert.assertTrue(student.next(), "Студент не найден");
        Assert.assertEquals(student.getString(full_name), "Баранова Анна Игоревна");
        Assert.assertEquals(student.getString(student_ticket_number), "ИСС-1001");
        Assert.assertNotNull(student.getString(password_hash));
        Assert.assertEquals(student.getString(group_name), "ИСС9-124");
        Assert.assertEquals(student.getString(role_name), "Students");
    }

    @Test(description = "Негативный кейс: Получение несуществующего студента по ID")
    public void getNonExistingStudentByIdTest() throws SQLException {
        ResultSet student = getStudentById(999);
        Assert.assertFalse(student.next(), "Студент не должен существовать");
    }

    @Test(description = "Позитивный кейс: Получение студента по номеру студенческого билета")
    public void getStudentByTicketNumberTest() throws SQLException {
        ResultSet student = getStudentByTicketNumber("ИСС-1001");
        Assert.assertTrue(student.next(), "Студент не найден");
        Assert.assertEquals(student.getString(full_name), "Баранова Анна Игоревна");
        Assert.assertEquals(student.getInt("student_id"), 1);
    }

    @Test(description = "Негативный кейс: Получение студента по несуществующему номеру билета")
    public void getStudentByNonExistingTicketNumberTest() throws SQLException {
        ResultSet student = getStudentByTicketNumber("НЕСУЩЕСТВУЮЩИЙ");
        Assert.assertFalse(student.next(), "Студент не должен существовать");
    }

    @Test(description = "Позитивный кейс: Проверка общего количества студентов")
    public void getStudentCountTest() throws SQLException {
        int studentCount = getStudentCount();
        Assert.assertEquals(studentCount, 30, "Количество студентов не совпадает");
    }

    @Test(description = "Позитивный кейс: Проверка существования студента")
    public void studentExistsTest() throws SQLException {
        boolean exists = studentExists(1);
        Assert.assertTrue(exists, "Студент должен существовать");
    }

    @Test(description = "Негативный кейс: Проверка отсутствия студента")
    public void studentNotExistsTest() throws SQLException {
        boolean exists = studentExists(999);
        Assert.assertFalse(exists, "Студент не должен существовать");
    }

    @Test(description = "Позитивный кейс: Получение всех студентов и проверка структуры данных")
    public void getAllStudentsTest() throws SQLException {
        ResultSet students = getAllStudents();
        int count = 0;
        while (students.next()) {
            count++;
            Assert.assertNotNull(students.getString(full_name));
            Assert.assertNotNull(students.getString(student_ticket_number));
            Assert.assertNotNull(students.getString(password_hash));
            Assert.assertNotNull(students.getString("group_id"));
            Assert.assertNotNull(students.getString("role_id"));
        }

        Assert.assertEquals(count, 30, "Количество студентов не совпадает");
    }

    @Test(description = "Позитивный кейс: Проверка данных конкретного студента")
    public void checkSpecificStudentDataTest() throws SQLException {
        ResultSet student = getStudentById(14);
        Assert.assertTrue(student.next(), "Студент не найден");
        Assert.assertEquals(student.getString(full_name), "Кудряшов Алексей Евгеньевич");
        Assert.assertEquals(student.getString(student_ticket_number), "ИСС-1014");
        Assert.assertEquals(student.getInt("group_id"), 1);
        Assert.assertEquals(student.getInt("role_id"), 2);
    }

    @Test(description = "Позитивный кейс: Проверка формата хэша пароля студента")
    public void checkPasswordHashFormatTest() throws SQLException {
        ResultSet student = getStudentById(1);
        Assert.assertTrue(student.next(), "Студент не найден");
        String passwordHash = student.getString(password_hash);
        Assert.assertNotNull(passwordHash);
        Assert.assertTrue(passwordHash.length() > 50);
        Assert.assertTrue(passwordHash.startsWith("$2a$"));
    }
}