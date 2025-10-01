package com.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
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
     * @param semester семестр для фильтрации (может быть null)
     * @return структура содержащая следующие ключи:
     *         - "gradesData": данные об оценках по предметам
     *         - "attendanceData": данные о посещаемости по предметам
     *         - "overallAvgGrade": средний балл по всем предметам
     *         - "overallAttendance": общий процент посещаемости
     * @throws IllegalArgumentException если studentId равен null
     */
    public Map<String, Object> getSummaryData(Long studentId, Integer semester) {
        if (studentId == null) {
            throw new IllegalArgumentException();
        }

        Map<String, Object> summary = new HashMap<>();

        Map<Long, Map<String, Object>> gradesData = gradeService.getGradesDashboard(studentId, semester);
        summary.put("gradesData", gradesData);

        Map<Long, Map<String, Object>> attendanceData = attendanceService.getAttendanceDashboard(studentId, semester);
        summary.put("attendanceData", attendanceData);

        double overallAvgGrade = calculateOverallAvgGrade(gradesData);
        summary.put("overallAvgGrade", overallAvgGrade);

        double overallAttendance = calculateOverallAttendance(attendanceData);
        summary.put("overallAttendance", overallAttendance);

        return summary;
    }

    /**
     * Формирует сводные данные об успеваемости и посещаемости студента для текущего семестра.
     *
     * @param studentId идентификатор студента для получения данных
     * @return структура с сводными данными
     */
    public Map<String, Object> getSummaryData(Long studentId) {
        Integer currentSemester = getCurrentSemester();
        return getSummaryData(studentId, currentSemester);
    }

    /**
     * Вычисляет текущий семестр на основе текущей даты.
     *
     * @return номер текущего семестра (1 или 2)
     */
    private Integer getCurrentSemester() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        return (month >= 2 && month <= 7) ? 2 : 1;
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

        for (Map.Entry<Long, Map<String, Object>> entry : gradesData.entrySet()) {
            Object avgObj = entry.getValue().get("avgGrade");
            if (avgObj != null) {
                if (avgObj instanceof Double) {
                    Double d = (Double) avgObj;
                    if (d > 0) {
                        sum += d;
                        count++;
                    }
                } else if (avgObj instanceof Integer) {
                    Integer i = (Integer) avgObj;
                    if (i > 0) {
                        sum += i.doubleValue();
                        count++;
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

        for (Map.Entry<Long, Map<String, Object>> entry : attendanceData.entrySet()) {
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
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }
}