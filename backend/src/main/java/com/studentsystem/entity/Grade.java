package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Grade {
    private Long id;
    private String name;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}