package com.studentsystem.service;

import com.studentsystem.entity.Question;
import com.studentsystem.entity.StudentExerciseAnswer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StudentExerciseService {
    
    /**
     * 提交学生答案并开始自动批改
     * @param studentId 学生ID
     * @param questionId 题目ID
     * @param answer 学生答案
     * @param answerType 答案类型：1=文本，2=图片
     * @param imageUrls 图片URL列表（JSON格式）
     * @return 批改结果
     */
    StudentExerciseAnswer submitAnswer(Long studentId, Long questionId, String answer, Integer answerType, String imageUrls);
    
    /**
     * 自动批改题目（客观题立即批改，主观题异步批改）
     * @param studentId 学生ID
     * @param question 题目
     * @param studentAnswer 学生答案
     * @return 批改结果
     */
    StudentExerciseAnswer autoGradeQuestion(Long studentId, Question question, StudentExerciseAnswer studentAnswer);
    
    /**
     * 批改客观题（选择题和填空题）
     * @param question 题目
     * @param studentAnswer 学生答案
     * @return 批改结果
     */
    StudentExerciseAnswer gradeObjectiveQuestion(Question question, StudentExerciseAnswer studentAnswer);
    
    /**
     * 异步批改主观题（简答/计算题）
     * @param question 题目
     * @param studentAnswer 学生答案
     * @return CompletableFuture批改结果
     */
    CompletableFuture<StudentExerciseAnswer> gradeSubjectiveQuestionAsync(Question question, StudentExerciseAnswer studentAnswer);
    
    /**
     * 调用AI接口批改主观题
     * @param question 题目
     * @param studentAnswer 学生答案
     * @return 批改结果
     */
    StudentExerciseAnswer gradeSubjectiveQuestionWithAI(Question question, StudentExerciseAnswer studentAnswer);
    
    /**
     * 获取学生的批改结果
     * @param studentId 学生ID
     * @param exerciseSetId 习题集ID
     * @return 批改结果列表
     */
    List<StudentExerciseAnswer> getGradingResults(Long studentId, Long exerciseSetId);
    
    /**
     * 根据题目ID获取批改结果
     * @param studentId 学生ID
     * @param questionId 题目ID
     * @return 批改结果
     */
    StudentExerciseAnswer getGradingResultByQuestionId(Long studentId, Long questionId);
}