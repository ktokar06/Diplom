package com.example.service;

import java.sql.*;

/**
 * Сервис для работы с данными студентов в базе данных
 * Предоставляет методы для получения информации о студентах
 */
public class StudentServiceInTest {

    private static Connection connection;

    /**
     * Устанавливает соединение с базой данных для сервиса
     * @param connection соединение с базой данных
     */
    public static void setConnection(Connection connection) {
        StudentServiceInTest.connection = connection;
    }

    /**
     * Получает студента по идентификатору
     * @param studentId идентификатор студента
     * @return ResultSet с данными студента
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getStudentById(int studentId) throws SQLException {
        String query = "SELECT s.*, g.group_name, r.role_name FROM students s " +
                "LEFT JOIN groups g ON s.group_id = g.group_id " +
                "LEFT JOIN roles r ON s.role_id = r.role_id " +
                "WHERE s.student_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, studentId);
        return pstmt.executeQuery();
    }

    /**
     * Получает всех студентов из базы данных
     * @return ResultSet со всеми студентами
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getAllStudents() throws SQLException {
        String query = "SELECT s.*, g.group_name, r.role_name FROM students s " +
                "LEFT JOIN groups g ON s.group_id = g.group_id " +
                "LEFT JOIN roles r ON s.role_id = r.role_id " +
                "ORDER BY s.student_id";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    /**
     * Получает студента по номеру студенческого билета
     * @param ticketNumber номер студенческого билета
     * @return ResultSet с данными студента
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static ResultSet getStudentByTicketNumber(String ticketNumber) throws SQLException {
        String query = "SELECT s.*, g.group_name, r.role_name FROM students s " +
                "LEFT JOIN groups g ON s.group_id = g.group_id " +
                "LEFT JOIN roles r ON s.role_id = r.role_id " +
                "WHERE s.student_ticket_number = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, ticketNumber);
        return pstmt.executeQuery();
    }

    /**
     * Получает общее количество студентов в базе данных
     * @return количество студентов
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static int getStudentCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM students";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    /**
     * Проверяет существование студента по идентификатору
     * @param studentId идентификатор студента
     * @return true если студент существует, false в противном случае
     * @throws SQLException если произошла ошибка при работе с базой данных
     */
    public static boolean studentExists(int studentId) throws SQLException {
        String query = "SELECT 1 FROM students WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}