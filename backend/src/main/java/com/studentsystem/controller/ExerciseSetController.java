package com.studentsystem.controller;

import com.studentsystem.entity.ExerciseSet;
import com.studentsystem.entity.Question;
import com.studentsystem.service.ExerciseSetService;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            List<ExerciseSet> exerciseSets = exerciseSetService.getExerciseSetsByCreatorId(teacherId);
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
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // 设置创建者信息
            exerciseSet.setCreatorId(teacherId);
            
            // 从token中获取创建者姓名
            String creatorName = jwtUtil.getRealNameFromRequest(request);
            exerciseSet.setCreatorName(creatorName);
            
            ExerciseSet created = exerciseSetService.createExerciseSet(exerciseSet);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取习题集详情
     * @param id 习题集ID
     * @param request HTTP请求
     * @return 习题集详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getExerciseSet(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(id);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            response.put("success", true);
            response.put("data", exerciseSet);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取习题集详情失败: " + e.getMessage());
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
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet existing = exerciseSetService.getExerciseSetById(id);
            if (existing == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            boolean success = exerciseSetService.updateExerciseSet(id, exerciseSet);
            if (success) {
                response.put("success", true);
                response.put("message", "更新成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "更新失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 发布习题集
     * @param id 习题集ID
     * @param request HTTP请求
     * @return 发布结果
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishExerciseSet(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(id);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            boolean success = exerciseSetService.publishExerciseSet(id);
            if (success) {
                response.put("success", true);
                response.put("message", "发布成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "发布失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
    
    /**
     * 取消发布习题集
     * @param id 习题集ID
     * @param request HTTP请求
     * @return 取消发布结果
     */
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Map<String, Object>> unpublishExerciseSet(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(id);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            boolean success = exerciseSetService.unpublishExerciseSet(id);
            if (success) {
                response.put("success", true);
                response.put("message", "取消发布成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "取消发布失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "取消发布失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 删除习题集
     * @param id 习题集ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteExerciseSet(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(id);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            boolean success = exerciseSetService.deleteExerciseSet(id);
            if (success) {
                response.put("success", true);
                response.put("message", "删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "删除失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取习题集的题目列表
     * @param exerciseSetId 习题集ID
     * @param request HTTP请求
     * @return 题目列表
     */
    @GetMapping("/{exerciseSetId}/questions")
    public ResponseEntity<Map<String, Object>> getQuestions(
            @PathVariable Long exerciseSetId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
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
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            // 设置习题集ID
            question.setExerciseSetId(exerciseSetId);
            
            Question created = exerciseSetService.addQuestion(question);
            response.put("success", true);
            response.put("message", "添加成功");
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 更新题目
     * @param exerciseSetId 习题集ID
     * @param id 题目ID
     * @param question 题目信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/{exerciseSetId}/questions/{id}")
    public ResponseEntity<Map<String, Object>> updateQuestion(
            @PathVariable Long exerciseSetId,
            @PathVariable Long id,
            @RequestBody Question question,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            Question existingQuestion = exerciseSetService.getQuestionById(id);
            if (existingQuestion == null) {
                response.put("success", false);
                response.put("message", "题目不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            // 确保题目属于指定的习题集
            if (!existingQuestion.getExerciseSetId().equals(exerciseSetId)) {
                response.put("success", false);
                response.put("message", "无权限访问");
                return ResponseEntity.status(403).body(response);
            }
            
            question.setId(id);
            question.setExerciseSetId(exerciseSetId);
            
            boolean success = exerciseSetService.updateQuestion(id, question);
            if (success) {
                response.put("success", true);
                response.put("message", "更新成功");
                // 获取更新后的题目信息
                Question updated = exerciseSetService.getQuestionById(id);
                response.put("data", updated);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "更新失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 删除题目
     * @param exerciseSetId 习题集ID
     * @param id 题目ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{exerciseSetId}/questions/{id}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(
            @PathVariable Long exerciseSetId,
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            Question existingQuestion = exerciseSetService.getQuestionById(id);
            if (existingQuestion == null) {
                response.put("success", false);
                response.put("message", "题目不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            // 确保题目属于指定的习题集
            if (!existingQuestion.getExerciseSetId().equals(exerciseSetId)) {
                response.put("success", false);
                response.put("message", "无权限访问");
                return ResponseEntity.status(403).body(response);
            }
            
            boolean success = exerciseSetService.deleteQuestion(id);
            if (success) {
                response.put("success", true);
                response.put("message", "删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "删除失败");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 批量保存题目
     * @param exerciseSetId 习题集ID
     * @param questions 题目列表
     * @param request HTTP请求
     * @return 保存结果
     */
    @PostMapping("/{exerciseSetId}/questions/batch")
    public ResponseEntity<Map<String, Object>> saveQuestions(
            @PathVariable Long exerciseSetId,
            @RequestBody List<Question> questions,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            ExerciseSet exerciseSet = exerciseSetService.getExerciseSetById(exerciseSetId);
            if (exerciseSet == null) {
                response.put("success", false);
                response.put("message", "习题集不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            // 批量保存题目
            List<Question> savedQuestions = new ArrayList<>();
            int sortOrder = 1;
            for (Question question : questions) {
                question.setExerciseSetId(exerciseSetId);
                question.setSortOrder(sortOrder++);
                Question saved = exerciseSetService.addQuestion(question);
                savedQuestions.add(saved);
            }
            
            response.put("success", true);
            response.put("message", "题目保存成功，共保存" + savedQuestions.size() + "道题目");
            response.put("data", savedQuestions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "保存题目失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}