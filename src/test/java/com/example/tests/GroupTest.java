package com.example.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.service.GroupService.*;

public class GroupTest extends BaseTest {

    private final String group_name = "group_name";
    private final String speciality_code = "speciality_code";
    private final String department_name = "department_name";

    @Test(description = "Позитивный кейс: Получение существующей группы по ID")
    public void getExistingGroupByIdTest() throws SQLException {
        ResultSet group = getGroupById(1);
        Assert.assertTrue(group.next(), "Группа не найдена");
        Assert.assertEquals(group.getString(group_name), "ИСС9-124");
        Assert.assertEquals(group.getString(speciality_code), "11.02.15");
        Assert.assertEquals(group.getString(department_name), "Инфокоммуникационные сети и системы связи");
    }

    @Test(description = "Негативный кейс: Получение несуществующей группы по ID")
    public void getNonExistingGroupByIdTest() throws SQLException {
        ResultSet group = getGroupById(999);
        Assert.assertFalse(group.next(), "Группа не должна существовать");
    }

    @Test(description = "Позитивный кейс: Проверка общего количества групп")
    public void getGroupCountTest() throws SQLException {
        int groupCount = getGroupCount();
        Assert.assertEquals(groupCount, 2, "Количество групп не совпадает");
    }

    @Test(description = "Позитивный кейс: Проверка существования группы")
    public void groupExistsTest() throws SQLException {
        boolean exists = groupExists(1);
        Assert.assertTrue(exists, "Группа должна существовать");
    }

    @Test(description = "Негативный кейс: Проверка отсутствия группы")
    public void groupNotExistsTest() throws SQLException {
        boolean exists = groupExists(999);
        Assert.assertFalse(exists, "Группа не должна существовать");
    }

    @Test(description = "Позитивный кейс: Получение всех групп и проверка структуры данных")
    public void getAllGroupsTest() throws SQLException {
        ResultSet groups = getAllGroups();
        int count = 0;
        while (groups.next()) {
            count++;
            Assert.assertNotNull(groups.getString(group_name));
            Assert.assertNotNull(groups.getString(speciality_code));
            Assert.assertNotNull(groups.getString(department_name));
        }
        Assert.assertEquals(count, 2, "Количество групп не совпадает");
    }

    @Test(description = "Позитивный кейс: Проверка данных конкретной группы")
    public void checkSpecificGroupDataTest() throws SQLException {
        ResultSet group = getGroupById(2);
        Assert.assertTrue(group.next(), "Группа не найдена");
        Assert.assertEquals(group.getString(group_name), "ССА9-124А");
        Assert.assertEquals(group.getString(speciality_code), "09.02.06");
        Assert.assertEquals(group.getString(department_name), "Сетевое и системное администрирование");
    }
}