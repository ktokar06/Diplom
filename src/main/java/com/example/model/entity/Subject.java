package com.example.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "subject_name")
    private String name;

    @Column(name = "max_study_load")
    private Integer maxStudyLoad;

    @Column(name = "assessment_form")
    private String assessmentForm;

    @Column(name = "semester")
    private Integer semester;
}