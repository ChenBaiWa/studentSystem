package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Chapter {
    private Long id;
    private String name;
    private Long textbookId;
    private String textbookName;
    private Long creatorId;
    private String creatorName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}