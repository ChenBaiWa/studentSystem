package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentClass {
    private Long id;
    private Long studentId;
    private Long classId;
    private LocalDateTime joinTime;
}