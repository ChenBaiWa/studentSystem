package com.studentsystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExerciseSetChapterDTO {
    private Long id;
    private String name;
    private Long exerciseSetId;
    private String exerciseSetName;
    private Integer questionCount;
    private Integer totalScore;
    private LocalDateTime createTime;
}