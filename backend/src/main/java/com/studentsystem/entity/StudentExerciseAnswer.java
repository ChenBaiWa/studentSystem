package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class StudentExerciseAnswer {
    private Long id;
    private Long studentId; // 学生ID
    private Long questionId; // 题目ID
    private String answer; // 学生答案
    private Integer answerType; // 答案类型：1=文本，2=图片
    private String imageUrls; // 图片URL列表（JSON格式存储）
    private Integer score; // 得分
    private String remark; // 批注
    private Integer correctStatus; // 是否正确：0=未批改，1=正确，2=错误
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}