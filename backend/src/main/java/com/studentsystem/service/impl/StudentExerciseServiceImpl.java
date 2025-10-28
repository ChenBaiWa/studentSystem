package com.studentsystem.service.impl;

import com.studentsystem.entity.Question;
import com.studentsystem.entity.StudentExerciseAnswer;
import com.studentsystem.mapper.QuestionMapper;
import com.studentsystem.mapper.StudentExerciseAnswerMapper;
import com.studentsystem.service.StudentExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class StudentExerciseServiceImpl implements StudentExerciseService {
    
    @Autowired
    private StudentExerciseAnswerMapper studentExerciseAnswerMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Override
    public StudentExerciseAnswer submitAnswer(Long studentId, Long questionId, String answer, Integer answerType, String imageUrls) {
        // 查找是否已存在答题记录
        StudentExerciseAnswer existingAnswer = studentExerciseAnswerMapper.findByStudentIdAndQuestionId(studentId, questionId);
        
        StudentExerciseAnswer studentAnswer;
        if (existingAnswer != null) {
            // 更新现有记录
            studentAnswer = existingAnswer;
            studentAnswer.setAnswer(answer);
            studentAnswer.setAnswerType(answerType);
            studentAnswer.setImageUrls(imageUrls);
            studentAnswer.setUpdateTime(LocalDateTime.now());
            studentExerciseAnswerMapper.update(studentAnswer);
        } else {
            // 创建新记录
            studentAnswer = new StudentExerciseAnswer();
            studentAnswer.setStudentId(studentId);
            studentAnswer.setQuestionId(questionId);
            studentAnswer.setAnswer(answer);
            studentAnswer.setAnswerType(answerType);
            studentAnswer.setImageUrls(imageUrls);
            studentAnswer.setCreateTime(LocalDateTime.now());
            studentAnswer.setUpdateTime(LocalDateTime.now());
            studentExerciseAnswerMapper.insert(studentAnswer);
        }
        
        // 获取题目信息
        Question question = questionMapper.findById(questionId);
        if (question != null) {
            // 开始自动批改
            return autoGradeQuestion(studentId, question, studentAnswer);
        }
        
        return studentAnswer;
    }
    
    @Override
    public StudentExerciseAnswer autoGradeQuestion(Long studentId, Question question, StudentExerciseAnswer studentAnswer) {
        // 根据题型决定批改方式
        if ("choice".equals(question.getType()) || "fill".equals(question.getType())) {
            // 客观题立即批改
            return gradeObjectiveQuestion(question, studentAnswer);
        } else if ("subjective".equals(question.getType())) {
            // 主观题异步批改
            gradeSubjectiveQuestionAsync(question, studentAnswer);
            // 设置状态为批改中
            studentAnswer.setCorrectStatus(0); // 0表示未批改/批改中
            studentAnswer.setRemark("AI批改中...");
            studentAnswer.setUpdateTime(LocalDateTime.now());
            studentExerciseAnswerMapper.update(studentAnswer);
        }
        return studentAnswer;
    }
    
    @Override
    public StudentExerciseAnswer gradeObjectiveQuestion(Question question, StudentExerciseAnswer studentAnswer) {
        String correctAnswer = question.getAnswer();
        String studentAns = studentAnswer.getAnswer();
        
        boolean isCorrect = false;
        String remark = "";
        
        if ("choice".equals(question.getType())) {
            // 选择题批改
            if (correctAnswer != null && correctAnswer.equalsIgnoreCase(studentAns)) {
                isCorrect = true;
                remark = "答案正确";
            } else {
                remark = "答案错误，正确答案：" + correctAnswer;
            }
        } else if ("fill".equals(question.getType())) {
            // 填空题批改（忽略大小写和空格）
            if (correctAnswer != null && 
                correctAnswer.replaceAll("\\s+", "").equalsIgnoreCase(
                    studentAns != null ? studentAns.replaceAll("\\s+", "") : "")) {
                isCorrect = true;
                remark = "答案正确";
            } else {
                remark = "答案错误，正确答案：" + correctAnswer;
            }
        }
        
        // 更新批改结果
        studentAnswer.setScore(isCorrect ? question.getScore() : 0);
        studentAnswer.setCorrectStatus(isCorrect ? 1 : 2); // 1=正确，2=错误
        studentAnswer.setRemark(remark);
        studentAnswer.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        studentExerciseAnswerMapper.updateGradingResult(studentAnswer);
        
        return studentAnswer;
    }
    
    @Override
    public CompletableFuture<StudentExerciseAnswer> gradeSubjectiveQuestionAsync(Question question, StudentExerciseAnswer studentAnswer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return gradeSubjectiveQuestionWithAI(question, studentAnswer);
            } catch (Exception e) {
                // 出现异常时，设置默认值
                studentAnswer.setScore(0);
                studentAnswer.setCorrectStatus(2); // 错误
                studentAnswer.setRemark("AI批改失败：" + e.getMessage());
                studentAnswer.setUpdateTime(LocalDateTime.now());
                studentExerciseAnswerMapper.updateGradingResult(studentAnswer);
                return studentAnswer;
            }
        }, executorService);
    }
    
    @Override
    public StudentExerciseAnswer gradeSubjectiveQuestionWithAI(Question question, StudentExerciseAnswer studentAnswer) {
        // 这里应该调用实际的AI接口进行批改
        // 由于AI接口实现较为复杂，这里提供一个模拟实现
        try {
            // 模拟AI批改过程
            Thread.sleep(2000); // 模拟AI处理时间
            
            // 模拟AI返回结果
            StudentExerciseAnswer result = new StudentExerciseAnswer();
            result.setId(studentAnswer.getId());
            // 简单的评分逻辑：如果学生答案包含关键词则给满分，否则给一半分数
            String answer = studentAnswer.getAnswer() != null ? studentAnswer.getAnswer().toLowerCase() : "";
            String standardAnswer = question.getAnswer() != null ? question.getAnswer().toLowerCase() : "";
            
            // 检查学生答案是否包含标准答案中的关键词
            boolean isCorrect = !answer.isEmpty() && standardAnswer.contains(answer) || answer.contains(standardAnswer);
            
            result.setScore(isCorrect ? question.getScore() : question.getScore() / 2);
            result.setCorrectStatus(isCorrect ? 1 : 2); // 1=正确，2=错误
            result.setRemark(isCorrect ? "回答正确" : "回答基本正确，但不够完整");
            result.setUpdateTime(LocalDateTime.now());
            
            // 更新数据库
            studentExerciseAnswerMapper.updateGradingResult(result);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("AI批改失败", e);
        }
    }
    
    @Override
    public List<StudentExerciseAnswer> getGradingResults(Long studentId, Long exerciseSetId) {
        // 直接通过Mapper查询学生的答题结果
        return studentExerciseAnswerMapper.findByStudentIdAndExerciseSetId(studentId, exerciseSetId);
    }
    
    @Override
    public StudentExerciseAnswer getGradingResultByQuestionId(Long studentId, Long questionId) {
        return studentExerciseAnswerMapper.findByStudentIdAndQuestionId(studentId, questionId);
    }
}