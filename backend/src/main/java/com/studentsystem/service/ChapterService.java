package com.studentsystem.service;

import com.studentsystem.entity.Chapter;
import java.util.List;

public interface ChapterService {
    
    /**
     * 根据课本ID查询章节列表
     * @param textbookId 课本ID
     * @return 章节列表
     */
    List<Chapter> findByTextbookId(Long textbookId);
    
    /**
     * 创建章节
     * @param chapter 章节信息
     * @return 创建的章节
     */
    Chapter createChapter(Chapter chapter);
    
    /**
     * 更新章节信息
     * @param chapter 章节信息
     * @return 更新后的章节信息
     */
    Chapter updateChapter(Chapter chapter);
    
    /**
     * 删除章节
     * @param id 章节ID
     */
    void deleteChapter(Long id);
    
    /**
     * 根据ID查询章节
     * @param id 章节ID
     * @return 章节信息
     */
    Chapter findById(Long id);
}