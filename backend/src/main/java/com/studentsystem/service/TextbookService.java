package com.studentsystem.service;

import com.studentsystem.entity.Textbook;

import java.util.List;

public interface TextbookService {
    
    /**
     * 根据年级ID和创建人ID获取课本列表
     */
    List<Textbook> getTextbooksByGradeIdAndCreatorId(Long gradeId, Long creatorId);
    
    /**
     * 创建课本
     */
    Textbook createTextbook(Textbook textbook);
    
    /**
     * 更新课本
     */
    Textbook updateTextbook(Long id, Textbook textbook);
    
    /**
     * 删除课本
     */
    boolean deleteTextbook(Long id);
    
    /**
     * 根据ID获取课本
     */
    Textbook getTextbookById(Long id);
}