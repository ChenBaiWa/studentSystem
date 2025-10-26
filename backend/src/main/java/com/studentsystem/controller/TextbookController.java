package com.studentsystem.controller;

import com.studentsystem.entity.Textbook;
import com.studentsystem.service.TextbookService;
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
@RequestMapping("/textbooks")
@RequiredArgsConstructor
public class TextbookController {
    
    private final TextbookService textbookService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 根据年级ID和创建人ID获取课本列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTextbooks(
            @RequestParam Long gradeId,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long creatorId = jwtUtil.getUserIdFromRequest(request);
            List<Textbook> textbooks = textbookService.getTextbooksByGradeIdAndCreatorId(gradeId, creatorId);
            response.put("success", true);
            response.put("data", textbooks);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 根据ID获取课本
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTextbookById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Textbook textbook = textbookService.getTextbookById(id);
            if (textbook != null) {
                response.put("success", true);
                response.put("data", textbook);
            } else {
                response.put("success", false);
                response.put("message", "课本不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 创建课本
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTextbook(@RequestBody Textbook textbook, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 从token中获取创建者信息
            Long creatorId = jwtUtil.getUserIdFromRequest(request);
            String creatorName = jwtUtil.getRealNameFromRequest(request);
            
            textbook.setCreatorId(creatorId);
            textbook.setCreatorName(creatorName);
            
            Textbook createdTextbook = textbookService.createTextbook(textbook);
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("data", createdTextbook);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新课本
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTextbook(
            @PathVariable Long id,
            @RequestBody Textbook textbook,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Textbook updatedTextbook = textbookService.updateTextbook(id, textbook);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", updatedTextbook);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除课本
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTextbook(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = textbookService.deleteTextbook(id);
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}