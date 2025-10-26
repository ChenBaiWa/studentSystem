package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Class {
    private Long id;
    private String name;
    private String classCode; // 班级号(6位数字)
    private String verificationCode; // 验证码(4位数字)
    private Long gradeId;
    private String gradeName;
    private Long creatorId; // 创建人ID(老师ID)
    private String creatorName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}