package com.example.model.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AttendanceRequest {
    private Long studentId;
    private Long subjectId;
    private LocalDate date;
    private Boolean isPresent;
    private String comment;
}