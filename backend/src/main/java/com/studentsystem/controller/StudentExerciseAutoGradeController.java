package com.studentsystem.controller;

import com.studentsystem.entity.Question;
import com.studentsystem.entity.StudentExerciseAnswer;
import com.studentsystem.service.ExerciseSetService;
import com.studentsystem.service.StudentExerciseService;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/exercise-auto-grade")
@CrossOrigin
public class StudentExerciseAutoGradeController {
    
    @Autowired
    private StudentExerciseService studentExerciseService;
    
    @Autowired
    private ExerciseSetService exerciseSetService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 提交答案并开始自动批改
     */
    @PostMapping("/submit-answer")
    public ResponseEntity<Map<String, Object>> submitAnswer(
            HttpServletRequest request,
            @RequestBody SubmitAnswerRequest submitAnswerRequest) {
        
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            StudentExerciseAnswer result = studentExerciseService.submitAnswer(
                    studentId,
                    submitAnswerRequest.getQuestionId(),
                    submitAnswerRequest.getAnswer(),
                    submitAnswerRequest.getAnswerType(),
                    submitAnswerRequest.getImageUrls()
            );
            
            response.put("success", true);
            response.put("message", "答案提交成功");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "答案提交失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取批改结果
     */
    @GetMapping("/result/{questionId}")
    public ResponseEntity<Map<String, Object>> getGradingResult(
            HttpServletRequest request,
            @PathVariable Long questionId) {
        
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            StudentExerciseAnswer result = studentExerciseService.getGradingResultByQuestionId(studentId, questionId);
            
            if (result == null) {
                response.put("success", false);
                response.put("message", "未找到批改结果");
                return ResponseEntity.status(404).body(response);
            }
            
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取批改结果失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取习题集的完整批改结果
     */
    @GetMapping("/results/{exerciseSetId}")
    public ResponseEntity<Map<String, Object>> getExerciseSetGradingResults(
            HttpServletRequest request,
            @PathVariable Long exerciseSetId) {
        
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // 获取学生答题结果
            List<StudentExerciseAnswer> results = studentExerciseService.getGradingResults(studentId, exerciseSetId);
            
            // 计算总分
            int totalScore = results.stream()
                    .mapToInt(answer -> answer.getScore() != null ? answer.getScore() : 0)
                    .sum();
            
            Map<String, Object> data = new HashMap<>();
            data.put("totalScore", totalScore);
            data.put("results", results);
            
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取批改结果失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 提交答案请求数据类
     */
    static class SubmitAnswerRequest {
        private Long questionId;
        private String answer;
        private Integer answerType;
        private String imageUrls;
        
        // Getters and Setters
        public Long getQuestionId() {
            return questionId;
        }
        
        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }
        
        public String getAnswer() {
            return answer;
        }
        
        public void setAnswer(String answer) {
            this.answer = answer;
        }
        
        public Integer getAnswerType() {
            return answerType;
        }
        
        public void setAnswerType(Integer answerType) {
            this.answerType = answerType;
        }
        
        public String getImageUrls() {
            return imageUrls;
        }
        
        public void setImageUrls(String imageUrls) {
            this.imageUrls = imageUrls;
        }
    }
}