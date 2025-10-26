package com.studentsystem.service;

import com.studentsystem.entity.Grade;

import java.util.List;

public interface GradeService {
    
    /**
     * 根据创建人ID获取年级列表
     */
    List<Grade> getGradesByCreatorId(Long creatorId);
    
    /**
     * 创建年级
     */
    Grade createGrade(Grade grade);
    
    /**
     * 更新年级
     */
    Grade updateGrade(Long id, Grade grade);
    
    /**
     * 删除年级
     */
    boolean deleteGrade(Long id);
    
    /**
     * 根据ID获取年级
     */
    Grade getGradeById(Long id);
}