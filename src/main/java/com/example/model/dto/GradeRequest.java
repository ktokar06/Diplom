package com.example.model.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class GradeRequest {
    private Long studentId;
    private Long subjectId;
    private Integer grade;
    private String workType;
    private String comment;
    private LocalDate assessmentDate;
}