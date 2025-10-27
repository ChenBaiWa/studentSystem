package com.studentsystem.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class Assignment {
    private Long id;
    private String title; // 作业标题
    private Long classId; // 班级ID
    private String className; // 班级名称
    private Long chapterId; // 章节ID
    private String chapterName; // 章节名称
    private String content; // 作业内容
    private Integer totalScore; // 总分
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline; // 截止时间
    private Long creatorId; // 创建人ID(老师ID)
    private String creatorName; // 创建人姓名
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
    private List<Long> classIds; // 班级ID列表（用于发布作业）
    private Integer submissionCount; // 提交人数
}