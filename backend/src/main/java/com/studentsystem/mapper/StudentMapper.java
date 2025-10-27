package com.studentsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studentsystem.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
    
    @Select("SELECT * FROM student WHERE id = #{id}")
    Student selectById(Long id);
    
    @Select("SELECT * FROM student WHERE id = #{userId}")
    Student selectByUserId(Long userId);
    
    @Insert("INSERT INTO student(id, name, gender, age, email, phone, address, create_time, update_time) " +
            "VALUES(#{id}, #{name}, #{gender}, #{age}, #{email}, #{phone}, #{address}, #{createTime}, #{updateTime})")
    int insert(Student student);
    
    @Update("UPDATE student SET name = #{name}, gender = #{gender}, age = #{age}, email = #{email}, phone = #{phone}, " +
            "address = #{address}, update_time = #{updateTime} WHERE id = #{id}")
    int update(Student student);
    
    @Delete("DELETE FROM student WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT s.* FROM student s JOIN student_class sc ON s.id = sc.student_id WHERE sc.class_id = #{classId}")
    List<Student> selectByClassId(Long classId);
    
    @Select("<script>" +
            "SELECT * FROM student WHERE id IN " +
            "<foreach item='item' collection='ids' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<Student> selectByIds(@Param("ids") List<Long> ids);
}