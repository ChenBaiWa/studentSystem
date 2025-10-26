package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Textbook {
    private Long id;
    private String name;
    private Long gradeId;
    private String gradeName; // 冗余字段，方便查询
    private Long creatorId;
    private String creatorName; // 冗余字段，方便查询
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}