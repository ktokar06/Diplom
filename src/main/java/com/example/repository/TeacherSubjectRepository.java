package com.example.repository;

import com.example.model.TeacherSubject;
import com.example.model.TeacherSubjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, TeacherSubjectId> {
    List<TeacherSubject> findBySubjectSubjectId(Long subjectId);
}