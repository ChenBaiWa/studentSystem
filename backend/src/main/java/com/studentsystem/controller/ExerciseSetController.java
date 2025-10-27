package com.studentsystem.controller;

import com.studentsystem.entity.ExerciseSet;
import com.studentsystem.entity.Question;
import com.studentsystem.service.ExerciseSetService;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exercise-sets")
@CrossOrigin
public class ExerciseSetController {
    
    @Autowired
    private ExerciseSetService exerciseSetService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取教师的习题集列表
     * @param request HTTP请求
     * @return 习题集列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getExerciseSets(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long creatorId = jwtUtil.getUserIdFromRequest(request);
            List<ExerciseSet> exerciseSets = exerciseSetService.getExerciseSetsByCreatorId(creatorId);
            response.put("success", true);
            response.put("data", exerciseSets);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取习题集列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 创建习题集
     * @param exerciseSet 习题集信息
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createExerciseSet(
            @RequestBody ExerciseSet exerciseSet, 
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long creatorId = jwtUtil.getUserIdFromRequest(request);
            String creatorName = jwtUtil.getRealNameFromRequest(request);
            
            exerciseSet.setCreatorId(creatorId);
            exerciseSet.setCreatorName(creatorName);
            
            ExerciseSet created = exerciseSetService.createExerciseSet(exerciseSet);
            response.put("success", true);
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建习题集失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 更新习题集
     * @param id 习题集ID
     * @param exerciseSet 习题集信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateExerciseSet(
            @PathVariable Long id,
            @RequestBody ExerciseSet exerciseSet,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = exerciseSetService.updateExerciseSet(id, exerciseSet);
            if (result) {
                response.put("success", true);
                response.put("message", "更新成功");
            } else {
                response.put("success", false);
                response.put("message", "更新失败，习题集不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新习题集失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取习题集详情
     * @param id 习题集ID
     * @return 习题集详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getExerciseSet(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(id);
            if (exerciseSet != null) {
                response.put("success", true);
                response.put("data", exerciseSet);
            } else {
                response.put("success", false);
                response.put("message", "习题集不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取习题集详情失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 发布习题集
     * @param id 习题集ID
     * @return 发布结果
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishExerciseSet(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = exerciseSetService.publishExerciseSet(id);
            if (result) {
                response.put("success", true);
                response.put("message", "发布成功");
            } else {
                response.put("success", false);
                response.put("message", "发布失败，习题集不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
    
    /**
     * 取消发布习题集
     * @param id 习题集ID
     * @return 取消发布结果
     */
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishExerciseSet(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = exerciseSetService.unpublishExerciseSet(id);
            if (result) {
                response.put("success", true);
                response.put("message", "取消发布成功");
            } else {
                response.put("success", false);
                response.put("message", "取消发布失败，习题集不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消发布失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 删除习题集
     * @param id 习题集ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteExerciseSet(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = exerciseSetService.deleteExerciseSet(id);
            if (result) {
                response.put("success", true);
                response.put("message", "删除成功");
            } else {
                response.put("success", false);
                response.put("message", "删除失败，习题集不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除习题集失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取习题集的题目列表
     * @param exerciseSetId 习题集ID
     * @return 题目列表
     */
    @GetMapping("/{exerciseSetId}/questions")
    public ResponseEntity<Map<String, Object>> getQuestions(@PathVariable Long exerciseSetId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Question> questions = exerciseSetService.getQuestionsByExerciseSetId(exerciseSetId);
            response.put("success", true);
            response.put("data", questions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取题目列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 添加题目
     * @param exerciseSetId 习题集ID
     * @param question 题目信息
     * @param request HTTP请求
     * @return 添加结果
     */
    @PostMapping("/{exerciseSetId}/questions")
    public ResponseEntity<Map<String, Object>> addQuestion(
            @PathVariable Long exerciseSetId,
            @RequestBody Question question,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            question.setExerciseSetId(exerciseSetId);
            question.setCreateTime(LocalDateTime.now());
            question.setUpdateTime(LocalDateTime.now());
            
            // 如果没有设置分值，默认为5分
            if (question.getScore() == null) {
                question.setScore(5);
            }
            
            Question created = exerciseSetService.addQuestion(question);
            response.put("success", true);
            response.put("data", created);
            response.put("message", "添加成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加题目失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 更新题目
     * @param exerciseSetId 习题集ID
     * @param id 题目ID
     * @param question 题目信息
     * @return 更新结果
     */
    @PutMapping("/{exerciseSetId}/questions/{id}")
    public ResponseEntity<Map<String, Object>> updateQuestion(
            @PathVariable Long exerciseSetId,
            @PathVariable Long id,
            @RequestBody Question question) {
        Map<String, Object> response = new HashMap<>();
        try {
            question.setId(id);
            question.setExerciseSetId(exerciseSetId);
            question.setUpdateTime(LocalDateTime.now());
            
            boolean result = exerciseSetService.updateQuestion(id, question);
            if (result) {
                response.put("success", true);
                response.put("message", "更新成功");
            } else {
                response.put("success", false);
                response.put("message", "更新失败，题目不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新题目失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 删除题目
     * @param exerciseSetId 习题集ID
     * @param id 题目ID
     * @return 删除结果
     */
    @DeleteMapping("/{exerciseSetId}/questions/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(
            @PathVariable Long exerciseSetId,
            @PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = exerciseSetService.deleteQuestion(id);
            if (result) {
                response.put("success", true);
                response.put("message", "删除成功");
            } else {
                response.put("success", false);
                response.put("message", "删除失败，题目不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除题目失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 批量保存题目
     * @param exerciseSetId 习题集ID
     * @param questions 题目列表
     * @return 保存结果
     */
    @PostMapping("/{exerciseSetId}/questions/batch")
    public ResponseEntity<Map<String, Object>> saveQuestions(
            @PathVariable Long exerciseSetId,
            @RequestBody List<Question> questions) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 先删除习题集下的所有题目
            List<Question> existingQuestions = exerciseSetService.getQuestionsByExerciseSetId(exerciseSetId);
            for (Question existingQuestion : existingQuestions) {
                exerciseSetService.deleteQuestion(existingQuestion.getId());
            }
            
            // 添加新题目
            for (Question question : questions) {
                question.setExerciseSetId(exerciseSetId);
                exerciseSetService.addQuestion(question);
            }
            
            response.put("success", true);
            response.put("message", "保存成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量保存题目失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}