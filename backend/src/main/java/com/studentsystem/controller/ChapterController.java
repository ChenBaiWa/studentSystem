package com.studentsystem.controller;

import com.studentsystem.entity.Chapter;
import com.studentsystem.service.ChapterService;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chapters")
public class ChapterController {
    
    @Autowired
    private ChapterService chapterService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 根据课本ID获取章节列表
     * @param textbookId 课本ID
     * @param request HTTP请求
     * @return 章节列表
     */
    @GetMapping
    public Map<String, Object> getChapters(@RequestParam Long textbookId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Chapter> chapters = chapterService.findByTextbookId(textbookId);
            result.put("success", true);
            result.put("data", chapters);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    /**
     * 创建章节
     * @param chapter 章节信息
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping
    public Map<String, Object> createChapter(@RequestBody Chapter chapter, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long creatorId = jwtUtil.getUserIdFromRequest(request);
            chapter.setCreatorId(creatorId);
            
            Chapter createdChapter = chapterService.createChapter(chapter);
            result.put("success", true);
            result.put("data", createdChapter);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    /**
     * 更新章节
     * @param chapter 章节信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping
    public Map<String, Object> updateChapter(@RequestBody Chapter chapter, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Chapter updatedChapter = chapterService.updateChapter(chapter);
            result.put("success", true);
            result.put("data", updatedChapter);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    /**
     * 删除章节
     * @param id 章节ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteChapter(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            chapterService.deleteChapter(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}