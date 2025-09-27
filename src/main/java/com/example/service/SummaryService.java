package com.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для формирования сводной информации об успеваемости и посещаемости студентов.
 * Предоставляет методы для агрегации данных об оценках и посещаемости в единую сводку.
 */
@Service
@RequiredArgsConstructor
public class SummaryService {

    private final GradeService gradeService;
    private final AttendanceService attendanceService;

    /**
     * Формирует сводные данные об успеваемости и посещаемости студента.
     * Включает данные об оценках, посещаемости, среднем балле и общем проценте посещаемости.
     *
     * @param studentId идентификатор студента для получения данных
     * @return карта, содержащая следующие ключи:
     *         - "gradesData": данные об оценках по предметам
     *         - "attendanceData": данные о посещаемости по предметам
     *         - "overallAvgGrade": средний балл по всем предметам
     *         - "overallAttendance": общий процент посещаемости
     * @throws IllegalArgumentException если studentId равен null
     */
    public Map<String, Object> getSummaryData(Long studentId) {
        Map<String, Object> summary = new HashMap<>();

        var gradesData = gradeService.getGradesDashboard(studentId);
        summary.put("gradesData", gradesData);

        var attendanceData = attendanceService.getAttendanceDashboard(studentId);
        summary.put("attendanceData", attendanceData);

        double overallAvgGrade = calculateOverallAvgGrade(gradesData);
        summary.put("overallAvgGrade", overallAvgGrade);

        double overallAttendance = calculateOverallAttendance(attendanceData);
        summary.put("overallAttendance", overallAttendance);

        return summary;
    }

    /**
     * Вычисляет средний балл по всем предметам на основе данных об оценках.
     * Игнорирует предметы без данных о среднем балле.
     *
     * @param gradesData данные оценок по предметам, где ключ - ID предмета,
     *                   значение - параметры включая "avgGrade"
     * @return средний балл по всем предметам или 0.0 если данные отсутствуют
     */
    private double calculateOverallAvgGrade(Map<Long, Map<String, Object>> gradesData) {
        double sum = 0.0;
        int count = 0;

        for (var entry : gradesData.entrySet()) {
            Object avgObj = entry.getValue().get("avgGrade");
            if (avgObj != null) {
                switch (avgObj) {
                    case Double d -> {
                        sum += d;
                        count++;
                    }
                    case Integer i -> {
                        sum += i.doubleValue();
                        count++;
                    }
                    default -> {
                    }
                }
            }
        }

        return count > 0 ? sum / count : 0.0;
    }

    /**
     * Вычисляет общий процент посещаемости студента по всем предметам.
     * Суммирует количество присутствий и общее количество занятий по всем предметам.
     *
     * @param attendanceData данные посещаемости по предметам, где ключ - ID предмета,
     *                       значение - параметры "present" и "total"
     * @return процент посещаемости в диапазоне от 0.0 до 100.0 или 0.0 если занятия отсутствуют
     */
    private double calculateOverallAttendance(Map<Long, Map<String, Object>> attendanceData) {
        long totalPresent = 0;
        long totalClasses = 0;

        for (var entry : attendanceData.entrySet()) {
            Object presentObj = entry.getValue().get("present");
            Object totalObj = entry.getValue().get("total");

            if (presentObj != null && totalObj != null) {
                long present = convertToLong(presentObj);
                long total = convertToLong(totalObj);

                totalPresent += present;
                totalClasses += total;
            }
        }

        if (totalClasses == 0) {
            return 0.0;
        }

        return (double) totalPresent / totalClasses * 100;
    }

    /**
     * Конвертирует объект в числовое значение типа long.
     * Поддерживает конвертацию из Long, Integer и String.
     *
     * @param obj объект для конвертации
     * @return числовое значение типа long; 0L если конвертация невозможна
     */
    private long convertToLong(Object obj) {
        return switch (obj) {
            case Long l -> l;
            case Integer i -> i.longValue();
            case String s -> {
                try {
                    yield Long.parseLong(s);
                } catch (NumberFormatException e) {
                    yield 0L;
                }
            }
            default -> 0L;
        };
    }
}