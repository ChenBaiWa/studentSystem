package com.studentsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentsystem.entity.Textbook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TextbookMapper extends BaseMapper<Textbook> {
    
    /**
     * 根据年级ID和创建人ID查询课本列表
     */
    List<Textbook> selectByTextbookList(@Param("gradeId") Long gradeId, @Param("creatorId") Long creatorId);
    
    /**
     * 统计同一创建人在同一_grade下是否存在相同名称的课本
     */
    int countByNameAndGradeIdAndCreatorId(@Param("name") String name, @Param("gradeId") Long gradeId, @Param("creatorId") Long creatorId);
    
    /**
     * 统计课本关联的章节数量
     */
    int countChaptersByTextbookId(@Param("textbookId") Long textbookId);
    
    /**
     * 根据ID查询课本
     */
    Textbook selectById(@Param("id") Long id);
}