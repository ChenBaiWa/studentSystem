package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class ExerciseSet {
    private Long id;
    private String name; // 习题集名称
    private String subject; // 学科
    private Long creatorId; // 创建人ID(老师ID)
    private String creatorName; // 创建人姓名
    private String status; // 状态: "editing"(编辑中) / "published"(已发布)
    private Integer questionCount; // 题目数量
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}