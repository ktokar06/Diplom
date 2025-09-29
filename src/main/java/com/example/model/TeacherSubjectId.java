package com.example.model;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectId implements Serializable {
    private Long teacherId;
    private Long subjectId;
}