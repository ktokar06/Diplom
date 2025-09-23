package com.example.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.service.RoleService.*;

public class RoleTest extends BaseTest {

    private final String role_name = "role_name";

    @Test(description = "Позитивный кейс: Получение существующей роли по ID")
    public void getExistingRoleByIdTest() throws SQLException {
        ResultSet role = getRoleById(1);
        Assert.assertTrue(role.next(), "Роль не найдена");
        Assert.assertEquals(role.getString(role_name), "Admins");
    }

    @Test(description = "Негативный кейс: Получение несуществующей роли по ID")
    public void getNonExistingRoleByIdTest() throws SQLException {
        ResultSet role = getRoleById(999);
        Assert.assertFalse(role.next(), "Роль не должна существовать");
    }

    @Test(description = "Позитивный кейс: Проверка общего количества ролей")
    public void getRoleCountTest() throws SQLException {
        int roleCount = getRoleCount();
        Assert.assertEquals(roleCount, 3, "Количество ролей не совпадает");
    }

    @Test(description = "Позитивный кейс: Проверка существования роли")
    public void roleExistsTest() throws SQLException {
        boolean exists = roleExists(1);
        Assert.assertTrue(exists, "Роль должна существовать");
    }

    @Test(description = "Негативный кейс: Проверка отсутствия роли")
    public void roleNotExistsTest() throws SQLException {
        boolean exists = roleExists(999);
        Assert.assertFalse(exists, "Роль не должна существовать");
    }

    @Test(description = "Позитивный кейс: Получение всех ролей и проверка структуры данных")
    public void getAllRolesTest() throws SQLException {
        ResultSet roles = getAllRoles();
        int count = 0;
        while (roles.next()) {
            count++;
            Assert.assertNotNull(roles.getString(role_name));
        }
        Assert.assertEquals(count, 3, "Количество ролей не совпадает");
    }

    @Test(description = "Позитивный кейс: Проверка данных всех ролей")
    public void checkAllRolesDataTest() throws SQLException {
        ResultSet roles = getAllRoles();

        String[] expectedRoles = {"Admins", "Students", "Teachers"};
        int index = 0;

        while (roles.next()) {
            Assert.assertEquals(roles.getString(role_name), expectedRoles[index]);
            index++;
        }
    }

    @Test(description = "Позитивный кейс: Проверка наличия системного администратора")
    public void checkAdminRoleExistsTest() throws SQLException {
        ResultSet role = getRoleById(1);
        Assert.assertTrue(role.next(), "Роль администратора не найдена");
        Assert.assertEquals(role.getString(role_name), "Admins", "Роль администратора должна существовать");
    }
}