package com.example.service;

import java.sql.*;

/**
 * Сервис для работы с данными ролей в базе данных
 * Предоставляет методы для получения информации о ролях пользователей
 */
public class RoleService {

    private static Connection connection;

    /**
     * Устанавливает соединение с базой данных для сервиса
     * @param connection соединение с базой данных
     */
    public static void setConnection(Connection connection) {
        RoleService.connection = connection;
    }

    /**
     * Получает роль по идентификатору
     * @param roleId идентификатор роли
     * @return ResultSet с данными роли
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getRoleById(int roleId) throws SQLException {
        String query = "SELECT * FROM roles WHERE role_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, roleId);
        return pstmt.executeQuery();
    }

    /**
     * Получает все роли из базы данных
     * @return ResultSet со всеми ролями
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getAllRoles() throws SQLException {
        String query = "SELECT * FROM roles ORDER BY role_id";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * Получает общее количество ролей в базе данных
     * @return количество ролей
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static int getRoleCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM roles";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    /**
     * Проверяет существование роли по идентификатору
     * @param roleId идентификатор роли
     * @return true если роль существует, false в противном случае
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static boolean roleExists(int roleId) throws SQLException {
        String query = "SELECT 1 FROM roles WHERE role_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}