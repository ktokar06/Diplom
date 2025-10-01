package com.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для формирования сводной информации об успеваемости и посещаемости студентов.
 * Предоставляет методы для агрегации данных об оценках и посещаемости в единую сводку.
 *
 * @author System
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class SummaryService {

    private final GradeService gradeService;
    private final AttendanceService attendanceService;

    /**
     * Формирует сводные данные об успеваемости и посещаемости студента за указанный семестр.
     * Включает данные об оценках, посещаемости, среднем балле и общем проценте посещаемости.
     *
     * @param studentId идентификатор студента для получения данных, не может быть null
     * @param semester семестр для фильтрации данных
     * @return Map содержащая следующие ключи:
     *         - "gradesData": данные об оценках по предметам
     *         - "attendanceData": данные о посещаемости по предметам
     *         - "overallAvgGrade": средний балл по всем предметам
     *         - "overallAttendance": общий процент посещаемости
     * @throws IllegalArgumentException если studentId равен null
     */
    public Map<String, Object> getSummaryData(Long studentId, Integer semester) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
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
     * Автоматически определяет текущий семестр на основе системной даты.
     *
     * @param studentId идентификатор студента для получения данных, не может быть null
     * @return Map с сводными данными за текущий семестр
     * @throws IllegalArgumentException если studentId равен null
     */
    public Map<String, Object> getSummaryData(Long studentId) {
        Integer currentSemester = getCurrentSemester();
        return getSummaryData(studentId, currentSemester);
    }

    /**
     * Получает данные по семестрам для построения графиков трендов успеваемости и посещаемости.
     * Собирает данные за все доступные семестры (1 и 2) для отображения динамики изменений.
     *
     * @param studentId идентификатор студента, не может быть null
     * @return Map с данными для графиков трендов:
     *         - "semesters": список номеров семестров [1, 2]
     *         - "grades": список средних баллов по семестрам
     *         - "attendance": список процентов посещаемости по семестрам
     */
    public Map<String, Object> getSemesterTrendData(Long studentId) {
        Map<String, Object> trendData = new HashMap<>();

        List<Double> semesterGrades = new ArrayList<>();
        List<Double> semesterAttendance = new ArrayList<>();
        List<Integer> semesters = new ArrayList<>();

        for (int semester = 1; semester <= 2; semester++) {
            Map<String, Object> semesterData = getSummaryData(studentId, semester);

            Double avgGrade = (Double) semesterData.get("overallAvgGrade");
            Double attendance = (Double) semesterData.get("overallAttendance");

            semesterGrades.add(avgGrade != null ? avgGrade : 0.0);
            semesterAttendance.add(attendance != null ? attendance : 0.0);
            semesters.add(semester);
        }

        trendData.put("semesters", semesters);
        trendData.put("grades", semesterGrades);
        trendData.put("attendance", semesterAttendance);

        return trendData;
    }

    /**
     * Вычисляет текущий семестр на основе текущей даты системы.
     * Семестр 1: август-январь (месяцы 8-1), семестр 2: февраль-июль (месяцы 2-7).
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
     * Игнорирует предметы без данных о среднем балле или с нулевыми значениями.
     *
     * @param gradesData данные оценок по предметам, где ключ - ID предмета,
     *                   значение - Map с параметрами включая "avgGrade"
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
     * Возвращает 0.0 если отсутствуют данные о занятиях.
     *
     * @param attendanceData данные посещаемости по предметам, где ключ - ID предмета,
     *                       значение - Map с параметрами "present" и "total"
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
     * Возвращает 0L если конвертация невозможна или объект имеет неподдерживаемый тип.
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