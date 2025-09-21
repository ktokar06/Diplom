package com.example.repository;

import com.example.model.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByStudentIdAndSubjectSubjectIdOrderByAttendanceDateDesc(Long studentId, Long subjectId);
}