package com.studentsystem.controller;

import com.studentsystem.entity.Grade;
import com.studentsystem.service.GradeService;
import com.studentsystem.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
public class GradeController {
    
    private final GradeService gradeService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 根据创建人ID获取年级列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGrades(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long creatorId = jwtUtil.getUserIdFromRequest(request);
            List<Grade> grades = gradeService.getGradesByCreatorId(creatorId);
            response.put("success", true);
            response.put("data", grades);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 创建年级
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGrade(@RequestBody Grade grade, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Grade createdGrade = gradeService.createGrade(grade);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("data", createdGrade);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新年级
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateGrade(
            @PathVariable Long id,
            @RequestBody Grade grade,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Grade updatedGrade = gradeService.updateGrade(id, grade);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updatedGrade);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除年级
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteGrade(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = gradeService.deleteGrade(id);
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}