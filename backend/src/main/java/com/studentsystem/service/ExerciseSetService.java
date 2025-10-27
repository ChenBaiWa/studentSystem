package com.studentsystem.service;

import com.studentsystem.entity.ExerciseSet;
import com.studentsystem.entity.Question;

import java.util.List;

public interface ExerciseSetService {
    
    /**
     * 创建习题集
     * @param exerciseSet 习题集信息
     * @return 创建的习题集
     */
    ExerciseSet createExerciseSet(ExerciseSet exerciseSet);
    
    /**
     * 获取教师的习题集列表
     * @param creatorId 教师ID
     * @return 习题集列表
     */
    List<ExerciseSet> getExerciseSetsByCreatorId(Long creatorId);
    
    /**
     * 获取习题集详情
     * @param id 习题集ID
     * @return 习题集详情
     */
    ExerciseSet getExerciseSetById(Long id);
    
    /**
     * 更新习题集
     * @param id 习题集ID
     * @param exerciseSet 习题集信息
     * @return 是否更新成功
     */
    boolean updateExerciseSet(Long id, ExerciseSet exerciseSet);
    
    /**
     * 发布习题集
     * @param id 习题集ID
     * @return 是否发布成功
     */
    boolean publishExerciseSet(Long id);
    
    /**
     * 取消发布习题集
     * @param id 习题集ID
     * @return 是否取消发布成功
     */
    boolean unpublishExerciseSet(Long id);
    
    /**
     * 删除习题集
     * @param id 习题集ID
     * @return 是否删除成功
     */
    boolean deleteExerciseSet(Long id);
    
    /**
     * 添加题目到习题集
     * @param question 题目信息
     * @return 添加的题目
     */
    Question addQuestion(Question question);
    
    /**
     * 获取习题集的题目列表
     * @param exerciseSetId 习题集ID
     * @return 题目列表
     */
    List<Question> getQuestionsByExerciseSetId(Long exerciseSetId);
    
    /**
     * 更新题目
     * @param id 题目ID
     * @param question 题目信息
     * @return 是否更新成功
     */
    boolean updateQuestion(Long id, Question question);
    
    /**
     * 删除题目
     * @param id 题目ID
     * @return 是否删除成功
     */
    boolean deleteQuestion(Long id);
    
    /**
     * 根据ID获取题目
     * @param id 题目ID
     * @return 题目信息
     */
    Question getQuestionById(Long id);
    
    /**
     * 获取所有已发布的习题集
     * @return 已发布的习题集列表
     */
    List<ExerciseSet> getPublishedExerciseSets();
    
    /**
     * 根据习题集ID和章节ID获取题目列表
     * @param exerciseSetId 习题集ID
     * @param chapterId 章节ID
     * @return 题目列表
     */
    List<Question> getQuestionsByExerciseSetIdAndChapterId(Long exerciseSetId, Long chapterId);
}