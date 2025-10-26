package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class StudentAssignment {
    private Long id;
    private Long assignmentId; // 作业ID
    private Long studentId; // 学生ID
    private String studentName; // 学生姓名
    private String answer; // 学生答案
    private Integer score; // 得分
    private String feedback; // 批注
    private String status; // 状态: "submitted"(已提交) / "graded"(已批改)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime; // 提交时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gradeTime; // 批改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}