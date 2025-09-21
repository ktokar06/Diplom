package com.example.repository;

import com.example.model.entity.AcademicPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AcademicPerformanceRepository extends JpaRepository<AcademicPerformance, Long> {
    List<AcademicPerformance> findByStudentId(Long studentId);
    List<AcademicPerformance> findByStudentIdAndSubjectSubjectId(Long studentId, Long subjectId);
}