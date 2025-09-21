package com.example.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "academic_performance")
public class AcademicPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long performanceId;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "assessment_date")
    private LocalDate assessmentDate;

    @Column(name = "work_type")
    private String workType;

    @Column(name = "comment")
    private String comment;
}