package com.studentsystem.mapper;

import com.studentsystem.entity.StudentClass;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentClassMapper {
    
    /**
     * 插入学生-班级关联记录
     * @param studentClass 学生班级关联信息
     * @return 影响行数
     */
    @Insert("INSERT INTO student_class(student_id, class_id, join_time) VALUES(#{studentId}, #{classId}, #{joinTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentClass studentClass);
    
    /**
     * 检查学生是否已加入指定班级
     * @param studentId 学生ID
     * @param classId 班级ID
     * @return 是否已加入
     */
    @Select("SELECT COUNT(*) > 0 FROM student_class WHERE student_id = #{studentId} AND class_id = #{classId}")
    boolean existsByStudentIdAndClassId(@Param("studentId") Long studentId, @Param("classId") Long classId);
    
    /**
     * 根据学生ID查询其加入的班级
     * @param studentId 学生ID
     * @return 学生班级关联列表
     */
    @Select("SELECT * FROM student_class WHERE student_id = #{studentId}")
    List<StudentClass> selectByStudentId(@Param("studentId") Long studentId);
}