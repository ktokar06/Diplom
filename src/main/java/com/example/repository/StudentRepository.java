package com.example.repository;

import com.example.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByGroupId(Long groupId);
    Optional<Student> findByStudentTicketNumber(String studentTicketNumber);
    Optional<Student> findById(Long id);
}