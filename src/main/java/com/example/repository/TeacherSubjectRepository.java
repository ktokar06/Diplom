package com.example.repository;

import com.example.model.entity.TeacherSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, com.example.model.entity.TeacherSubjectId> {
    List<TeacherSubject> findBySubjectSubjectId(Long subjectId);
}