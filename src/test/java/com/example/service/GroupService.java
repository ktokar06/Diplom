package com.example.service;

import java.sql.*;

/**
 * Сервис для работы с данными групп в базе данных
 * Предоставляет методы для получения информации о группах
 */
public class GroupService {

    private static Connection connection;

    /**
     * Устанавливает соединение с базой данных для сервиса
     * @param connection соединение с базой данных
     */
    public static void setConnection(Connection connection) {
        GroupService.connection = connection;
    }

    /**
     * Получает группу по идентификатору
     * @param groupId идентификатор группы
     * @return ResultSet с данными группы
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getGroupById(int groupId) throws SQLException {
        String query = "SELECT * FROM groups WHERE group_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, groupId);
        return pstmt.executeQuery();
    }

    /**
     * Получает все группы из базы данных
     * @return ResultSet со всеми группами
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getAllGroups() throws SQLException {
        String query = "SELECT * FROM groups ORDER BY group_id";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * Получает общее количество групп в базе данных
     * @return количество групп
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static int getGroupCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM groups";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    /**
     * Проверяет существование группы по идентификатору
     * @param groupId идентификатор группы
     * @return true если группа существует, false в противном случае
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static boolean groupExists(int groupId) throws SQLException {
        String query = "SELECT 1 FROM groups WHERE group_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}