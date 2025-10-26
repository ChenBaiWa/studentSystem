package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class Question {
    private Long id;
    private Long exerciseSetId; // 习题集ID
    private String type; // 题型: "choice"(选择题) / "fill"(填空题) / "subjective"(主观题)
    private String content; // 题干内容
    private String options; // 选项（JSON格式存储，适用于选择题）
    private String answer; // 标准答案
    private Integer score; // 分值
    private Integer sortOrder; // 排序
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}