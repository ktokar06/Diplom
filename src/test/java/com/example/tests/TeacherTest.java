package com.example.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.service.TeacherService.*;

public class TeacherTest extends BaseTest {

    private final String full_name = "full_name";
    private final String password_hash = "password_hash";
    private final String phone_number = "phone_number";
    private final String position = "position";
    private final String role_name = "role_name";

    @Test(description = "Позитивный кейс: Получение существующего преподавателя по ID")
    public void getExistingTeacherByIdTest() throws SQLException {
        ResultSet teacher = getTeacherById(1);
        Assert.assertTrue(teacher.next(), "Преподаватель не найден");
        Assert.assertEquals(teacher.getString(full_name), "Лазарева Надежда Владимировна");
        Assert.assertEquals(teacher.getString(phone_number), "+79001111111");
        Assert.assertEquals(teacher.getString(position), "Преподаватель");
        Assert.assertEquals(teacher.getString(role_name), "Teachers");
        Assert.assertNotNull(teacher.getString(password_hash));
    }

    @Test(description = "Негативный кейс: Получение несуществующего преподавателя по ID")
    public void getNonExistingTeacherByIdTest() throws SQLException {
        ResultSet teacher = getTeacherById(999);
        Assert.assertFalse(teacher.next(), "Преподаватель не должен существовать");
    }

    @Test(description = "Позитивный кейс: Проверка общего количества преподавателей")
    public void getTeacherCountTest() throws SQLException {
        int teacherCount = getTeacherCount();
        Assert.assertEquals(teacherCount, 15, "Количество преподавателей не совпадает");
    }

    @Test(description = "Позитивный кейс: Проверка существования преподавателя")
    public void teacherExistsTest() throws SQLException {
        boolean exists = teacherExists(1);
        Assert.assertTrue(exists, "Преподаватель должен существовать");
    }

    @Test(description = "Негативный кейс: Проверка отсутствия преподавателя")
    public void teacherNotExistsTest() throws SQLException {
        boolean exists = teacherExists(999);
        Assert.assertFalse(exists, "Преподаватель не должен существовать");
    }

    @Test(description = "Позитивный кейс: Получение всех преподавателей и проверка структуры данных")
    public void getAllTeachersTest() throws SQLException {
        ResultSet teachers = getAllTeachers();
        int count = 0;
        while (teachers.next()) {
            count++;
            Assert.assertNotNull(teachers.getString(full_name));
            Assert.assertNotNull(teachers.getString(password_hash));
            Assert.assertNotNull(teachers.getString(phone_number));
            Assert.assertNotNull(teachers.getString(position));
            Assert.assertNotNull(teachers.getString("role_id"));
        }
        Assert.assertEquals(count, 15, "Количество преподавателей не совпадает");
    }

    @Test(description = "Позитивный кейс: Проверка данных конкретного преподавателя")
    public void checkSpecificTeacherDataTest() throws SQLException {
        ResultSet teacher = getTeacherById(15);
        Assert.assertTrue(teacher.next(), "Преподаватель не найден");
        Assert.assertEquals(teacher.getString(full_name), "Качурин Игорь Анатольевич");
        Assert.assertEquals(teacher.getString(phone_number), "+79015555555");
        Assert.assertEquals(teacher.getString(position), "Преподаватель");
        Assert.assertEquals(teacher.getInt("role_id"), 3);
    }

    @Test(description = "Позитивный кейс: Проверка формата хэша пароля преподавателя")
    public void checkTeacherPasswordHashFormatTest() throws SQLException {
        ResultSet teacher = getTeacherById(1);
        Assert.assertTrue(teacher.next(), "Преподаватель не найден");
        String passwordHash = teacher.getString(password_hash);
        Assert.assertNotNull(passwordHash);
        Assert.assertTrue(passwordHash.length() > 50);
        Assert.assertTrue(passwordHash.startsWith("$2a$"));
    }
}