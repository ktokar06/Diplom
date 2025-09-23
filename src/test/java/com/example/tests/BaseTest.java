package com.example.tests;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseTest {

    protected static Connection connection;
    protected Statement statement;

    @BeforeClass
    public void setUp() {
        // Параметры подключения к БД PostgreSQL
        String hostname = "localhost";
        String dbname = "CT_MTUCI_DIPLOM_Kutsebo";
        String userName = "postgres";
        String password = "secret";

        createConnection(hostname, dbname, userName, password);
        createStatement();

        // Инициализируем сервисы с соединением
        initServices();
    }

    @AfterClass
    public void tearDown() {
        closeResources();
    }

    public void createConnection(String hostname, String dbname, String userName, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://" + hostname + ":5432/" + dbname;
            connection = DriverManager.getConnection(url, userName, password);
            System.out.println("Соединение с БД PostgreSQL установлено.");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Ошибка подключения к БД", e);
        }
    }

    public void createStatement() {
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании Statement", e);
        }
    }

    public void closeResources() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
                System.out.println("Соединение с БД закрыто.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при закрытии ресурсов", e);
        }
    }

    private void initServices() {
        com.example.service.GroupService.setConnection(connection);
        com.example.service.RoleService.setConnection(connection);
        com.example.service.StudentService.setConnection(connection);
        com.example.service.TeacherService.setConnection(connection);
    }
}