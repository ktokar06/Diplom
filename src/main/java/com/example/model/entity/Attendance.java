package com.example.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    @Column(name = "is_present")
    private Boolean isPresent;

    @Column(name = "comment")
    private String comment;

    @Column(name = "special_condition")
    private String specialCondition;

    @Column(name = "color_code")
    private String colorCode;
}