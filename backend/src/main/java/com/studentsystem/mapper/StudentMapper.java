package com.studentsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.studentsystem.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    
    /**
     * 根据姓名模糊查询
     */
    List<Student> selectByName(@Param("name") String name);
    
    /**
     * 根据性别查询
     */
    List<Student> selectByGender(@Param("gender") String gender);
    
    /**
     * 分页查询学生
     */
    IPage<Student> selectPage(IPage<Student> page);
    
    /**
     * 统计各性别学生数量
     */
    List<Map<String, Object>> countByGender();
    
    /**
     * 批量插入学生
     */
    int insertBatch(@Param("list") List<Student> students);
    
    /**
     * 批量更新学生
     */
    int updateBatch(@Param("list") List<Student> students);
    
    /**
     * 根据用户ID查询学生
     */
    @Select("SELECT * FROM student WHERE id = #{userId}")
    Student selectByUserId(@Param("userId") Long userId);
}