package com.studentsystem.mapper;

import com.studentsystem.entity.StudentClass;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentClassMapper {

    @Insert("INSERT INTO student_class(student_id, class_id, join_time) VALUES(#{studentId}, #{classId}, #{joinTime})")
    int insert(StudentClass studentClass);

    @Select("SELECT * FROM student_class WHERE student_id = #{studentId}")
    List<StudentClass> selectByStudentId(Long studentId);

    @Select("SELECT * FROM student_class WHERE class_id = #{classId}")
    List<StudentClass> selectByClassId(Long classId);

    @Delete("DELETE FROM student_class WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM student_class WHERE student_id = #{studentId} AND class_id = #{classId}")
    StudentClass selectByStudentIdAndClassId(@Param("studentId") Long studentId, @Param("classId") Long classId);
    @Select("SELECT COUNT(*) FROM student_class WHERE student_id = #{studentId} AND class_id = #{classId}")
    boolean existsByStudentIdAndClassId(@Param("studentId") Long studentId, @Param("classId") Long classId);

    @Select("<script>" +
            "SELECT * FROM student WHERE id IN " +
            "<foreach item='item' collection='ids' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<com.studentsystem.entity.Student> selectByIds(@Param("ids") List<Long> ids);
}
