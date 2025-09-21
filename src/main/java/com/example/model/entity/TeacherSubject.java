package com.example.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "teacher_subject")
@Data
public class TeacherSubject {
    @EmbeddedId
    private TeacherSubjectId id;

    @ManyToOne
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne
    @MapsId("subjectId")
    @JoinColumn(name = "subject_id")
    private Subject subject;
}