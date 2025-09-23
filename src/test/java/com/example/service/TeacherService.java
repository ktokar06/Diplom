package com.example.service;

import java.sql.*;

/**
 * Сервис для работы с данными преподавателей в базе данных
 * Предоставляет методы для получения информации о преподавателях
 */
public class TeacherService {

    private static Connection connection;

    /**
     * Устанавливает соединение с базой данных для сервиса
     * @param connection соединение с базой данных
     */
    public static void setConnection(Connection connection) {
        TeacherService.connection = connection;
    }

    /**
     * Получает преподавателя по идентификатору
     * @param teacherId идентификатор преподавателя
     * @return ResultSet с данными преподавателя
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getTeacherById(int teacherId) throws SQLException {
        String query = "SELECT t.*, r.role_name FROM teachers t " +
                "LEFT JOIN roles r ON t.role_id = r.role_id " +
                "WHERE t.teacher_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, teacherId);
        return pstmt.executeQuery();
    }

    /**
     * Получает всех преподавателей из базы данных
     * @return ResultSet со всеми преподавателями
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getAllTeachers() throws SQLException {
        String query = "SELECT t.*, r.role_name FROM teachers t " +
                "LEFT JOIN roles r ON t.role_id = r.role_id " +
                "ORDER BY t.teacher_id";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * Получает общее количество преподавателей в базе данных
     * @return количество преподавателей
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static int getTeacherCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM teachers";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    /**
     * Проверяет существование преподавателя по идентификатору
     * @param teacherId идентификатор преподавателя
     * @return true если преподаватель существует, false в противном случае
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static boolean teacherExists(int teacherId) throws SQLException {
        String query = "SELECT 1 FROM teachers WHERE teacher_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, teacherId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}