package com.studentsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentsystem.entity.Grade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GradeMapper extends BaseMapper<Grade> {
    
    /**
     * 根据创建人ID查询年级列表
     */
    List<Grade> selectByCreatorId(@Param("creatorId") Long creatorId);
}