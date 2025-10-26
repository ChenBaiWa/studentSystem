package com.studentsystem.service.impl;

import com.studentsystem.entity.ExerciseSet;
import com.studentsystem.entity.Question;
import com.studentsystem.mapper.ExerciseSetMapper;
import com.studentsystem.mapper.QuestionMapper;
import com.studentsystem.service.ExerciseSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExerciseSetServiceImpl implements ExerciseSetService {
    
    @Autowired
    private ExerciseSetMapper exerciseSetMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Override
    @Transactional
    public ExerciseSet createExerciseSet(ExerciseSet exerciseSet) {
        exerciseSet.setStatus("editing"); // 默认状态为编辑中
        exerciseSet.setQuestionCount(0); // 初始题目数量为0
        exerciseSet.setCreateTime(LocalDateTime.now());
        exerciseSet.setUpdateTime(LocalDateTime.now());
        exerciseSetMapper.insert(exerciseSet);
        return exerciseSet;
    }
    
    @Override
    public List<ExerciseSet> getExerciseSetsByCreatorId(Long creatorId) {
        List<ExerciseSet> exerciseSets = exerciseSetMapper.findByCreatorId(creatorId);
        // 更新每个习题集的题目数量
        for (ExerciseSet exerciseSet : exerciseSets) {
            int questionCount = exerciseSetMapper.countQuestionsByExerciseSetId(exerciseSet.getId());
            exerciseSet.setQuestionCount(questionCount);
        }
        return exerciseSets;
    }
    
    @Override
    public ExerciseSet getExerciseSetById(Long id) {
        ExerciseSet exerciseSet = exerciseSetMapper.findById(id);
        if (exerciseSet != null) {
            int questionCount = exerciseSetMapper.countQuestionsByExerciseSetId(exerciseSet.getId());
            exerciseSet.setQuestionCount(questionCount);
        }
        return exerciseSet;
    }
    
    @Override
    public boolean updateExerciseSet(Long id, ExerciseSet exerciseSet) {
        ExerciseSet existing = exerciseSetMapper.findById(id);
        if (existing == null) {
            return false;
        }
        
        existing.setName(exerciseSet.getName());
        existing.setSubject(exerciseSet.getSubject());
        existing.setUpdateTime(LocalDateTime.now());
        return exerciseSetMapper.update(existing) > 0;
    }
    
    @Override
    public boolean publishExerciseSet(Long id) {
        ExerciseSet exerciseSet = exerciseSetMapper.findById(id);
        if (exerciseSet == null) {
            return false;
        }
        
        // 检查是否有题目
        int questionCount = exerciseSetMapper.countQuestionsByExerciseSetId(id);
        if (questionCount == 0) {
            throw new RuntimeException("习题集至少需要包含一道题目才能发布");
        }
        
        return exerciseSetMapper.updateStatus(id, "published", LocalDateTime.now()) > 0;
    }
    
    @Override
    public boolean unpublishExerciseSet(Long id) {
        ExerciseSet exerciseSet = exerciseSetMapper.findById(id);
        if (exerciseSet == null) {
            return false;
        }
        
        return exerciseSetMapper.updateStatus(id, "editing", LocalDateTime.now()) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteExerciseSet(Long id) {
        ExerciseSet exerciseSet = exerciseSetMapper.findById(id);
        if (exerciseSet == null) {
            return false;
        }
        
        // 删除所有相关题目
        questionMapper.deleteByExerciseSetId(id);
        
        // 删除习题集
        return exerciseSetMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional
    public Question addQuestion(Question question) {
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        if (question.getScore() == null) {
            question.setScore(5); // 默认分值为5分
        }
        questionMapper.insert(question);
        
        // 更新习题集的题目数量
        ExerciseSet exerciseSet = exerciseSetMapper.findById(question.getExerciseSetId());
        if (exerciseSet != null) {
            exerciseSet.setQuestionCount(exerciseSet.getQuestionCount() + 1);
            exerciseSet.setUpdateTime(LocalDateTime.now());
            exerciseSetMapper.update(exerciseSet);
        }
        
        return question;
    }
    
    @Override
    public List<Question> getQuestionsByExerciseSetId(Long exerciseSetId) {
        return questionMapper.findByExerciseSetId(exerciseSetId);
    }
    
    @Override
    public boolean updateQuestion(Long id, Question question) {
        Question existing = questionMapper.findById(id);
        if (existing == null) {
            return false;
        }
        
        existing.setType(question.getType());
        existing.setContent(question.getContent());
        existing.setOptions(question.getOptions());
        existing.setAnswer(question.getAnswer());
        existing.setScore(question.getScore());
        existing.setUpdateTime(LocalDateTime.now());
        return questionMapper.update(existing) > 0;
    }
    
    @Override
    public boolean deleteQuestion(Long id) {
        Question question = questionMapper.findById(id);
        if (question == null) {
            return false;
        }
        
        boolean result = questionMapper.deleteById(id) > 0;
        
        // 更新习题集的题目数量
        if (result) {
            ExerciseSet exerciseSet = exerciseSetMapper.findById(question.getExerciseSetId());
            if (exerciseSet != null) {
                exerciseSet.setQuestionCount(Math.max(0, exerciseSet.getQuestionCount() - 1));
                exerciseSet.setUpdateTime(LocalDateTime.now());
                exerciseSetMapper.update(exerciseSet);
            }
        }
        
        return result;
    }
    
    @Override
    public Question getQuestionById(Long id) {
        return questionMapper.findById(id);
    }
}