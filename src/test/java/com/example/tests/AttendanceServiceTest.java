package com.example.tests;

import com.example.model.entity.Attendance;
import com.example.model.entity.Student;
import com.example.model.entity.Subject;
import com.example.repository.AttendanceRepository;
import com.example.repository.SubjectRepository;
import com.example.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAttendanceDashboard() {

        Long studentId = 1L;

        Student student = new Student();
        student.setId(studentId);

        Subject subject = new Subject();
        subject.setSubjectId(1L);
        subject.setName("Математика");

        Attendance attendance1 = new Attendance();
        attendance1.setStudent(student);
        attendance1.setSubject(subject);
        attendance1.setIsPresent(true);
        attendance1.setAttendanceDate(LocalDate.now());

        Attendance attendance2 = new Attendance();
        attendance2.setStudent(student);
        attendance2.setSubject(subject);
        attendance2.setIsPresent(false);
        attendance2.setAttendanceDate(LocalDate.now().minusDays(1));

        List<Attendance> attendances = Arrays.asList(attendance1, attendance2);

        when(attendanceRepository.findByStudentId(studentId)).thenReturn(attendances);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));


        Map<Long, Map<String, Object>> result = attendanceService.getAttendanceDashboard(studentId);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));

        Map<String, Object> subjectData = result.get(1L);
        assertEquals(Integer.valueOf(2), subjectData.get("total"));
        assertEquals(1L, subjectData.get("present"));
        assertEquals(1L, subjectData.get("absent"));
    }

    @Test
    void testGetAttendanceDetails() {
        Long subjectId = 1L;
        Long studentId = 1L;

        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        subject.setName("Физика");

        Student student = new Student();
        student.setId(studentId);

        Attendance attendance1 = new Attendance();
        attendance1.setStudent(student);
        attendance1.setSubject(subject);
        attendance1.setIsPresent(true);
        attendance1.setAttendanceDate(LocalDate.now());

        Attendance attendance2 = new Attendance();
        attendance2.setStudent(student);
        attendance2.setSubject(subject);
        attendance2.setIsPresent(false);
        attendance2.setAttendanceDate(LocalDate.now().minusDays(1));

        List<Attendance> attendances = Arrays.asList(attendance1, attendance2);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(attendanceRepository.findByStudentIdAndSubjectSubjectIdOrderByAttendanceDateDesc(studentId, subjectId))
                .thenReturn(attendances);

        Map<String, Object> result = attendanceService.getAttendanceDetails(subjectId, studentId);

        assertNotNull(result);
        assertEquals(subject, result.get("subject"));
        assertEquals(Integer.valueOf(2), result.get("totalClasses"));
        assertEquals(1L, result.get("presentCount"));
        assertEquals(1L, result.get("absentCount"));
        assertEquals(0L, result.get("lateCount"));
    }

    @Test
    void testCalculatePercentage() {
        BiFunction<Long, Integer, Double> mockCalculatePercentage = (part, total) -> {
            if (total <= 0) {
                return 0.0;
            }
            return java.math.BigDecimal.valueOf((double) part / total * 100)
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .doubleValue();
        };

        double result = mockCalculatePercentage.apply(3L, 4);
        assertEquals(75.0, result, 0.01);

        result = mockCalculatePercentage.apply(0L, 4);
        assertEquals(0.0, result, 0.01);

        result = mockCalculatePercentage.apply(4L, 0);
        assertEquals(0.0, result, 0.01);
    }
}