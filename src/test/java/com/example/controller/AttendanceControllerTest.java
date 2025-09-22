package com.example.controller;

import com.example.config.TestSecurityConfig;
import com.example.model.entity.Student;
import com.example.repository.StudentRepository;
import com.example.service.AttendanceService;
import com.example.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
@Import(TestSecurityConfig.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private GradeService gradeService;

    @MockBean
    private StudentRepository studentRepository;

    private Student mockStudent;

    @BeforeEach
    void setUp() {
        mockStudent = new Student();
        mockStudent.setId(1L);
        mockStudent.setFullName("Иванов Иван Иванович");
        mockStudent.setStudentTicketNumber("ST12345");

        when(studentRepository.findByStudentTicketNumber(anyString()))
                .thenReturn(Optional.of(mockStudent));
    }

    @Test
    @WithMockUser(username = "ST12345", roles = {"STUDENT"})
    void testAttendancePage() throws Exception {
        mockMvc.perform(get("/attendance"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/attendance/attendance"))
                .andExpect(model().attributeExists("fullName"))
                .andExpect(model().attribute("fullName", "Иванов Иван Иванович"));
    }

    @Test
    @WithMockUser(username = "ST12345", roles = {"STUDENT"})
    void testAttendanceDetailPage() throws Exception {
        when(attendanceService.getAttendanceDetails(1L, 1L)).thenReturn(null);

        mockMvc.perform(get("/attendance/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/attendance"));
    }
}