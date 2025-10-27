package com.studentsystem.mapper;

import com.studentsystem.entity.StudentAssignment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentAssignmentMapper {
    
    @Insert("INSERT INTO student_assignment(assignment_id, student_id, student_name, answer, score, feedback, status, submit_time, grade_time, create_time, update_time) " +
            "VALUES(#{assignmentId}, #{studentId}, #{studentName}, #{answer}, #{score}, #{feedback}, #{status}, #{submitTime}, #{gradeTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentAssignment studentAssignment);
    
    @Select("SELECT * FROM student_assignment WHERE id = #{id}")
    StudentAssignment findById(Long id);
    
    @Select("SELECT * FROM student_assignment WHERE assignment_id = #{assignmentId}")
    List<StudentAssignment> findByAssignmentId(Long assignmentId);
    
    @Select("SELECT * FROM student_assignment WHERE student_id = #{studentId} ORDER BY create_time DESC")
    List<StudentAssignment> findByStudentId(Long studentId);
    
    @Select("SELECT * FROM student_assignment WHERE assignment_id = #{assignmentId} AND student_id = #{studentId}")
    StudentAssignment selectByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId, @Param("studentId") Long studentId);
    
    @Update("UPDATE student_assignment SET answer = #{answer}, status = #{status}, submit_time = #{submitTime}, update_time = #{updateTime} WHERE id = #{id}")
    int update(StudentAssignment studentAssignment);
    
    @Update("UPDATE student_assignment SET score = #{score}, feedback = #{feedback}, status = #{status}, grade_time = #{gradeTime}, update_time = #{updateTime} WHERE id = #{id}")
    int grade(StudentAssignment studentAssignment);
    
    @Delete("DELETE FROM student_assignment WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT * FROM student_assignment WHERE assignment_id = #{assignmentId}")
    List<StudentAssignment> selectByAssignmentId(Long assignmentId);
    
    @Select("SELECT * FROM student_assignment WHERE student_id = #{studentId} ORDER BY create_time DESC")
    List<StudentAssignment> selectByStudentId(Long studentId);
    
    @Select("SELECT * FROM student_assignment WHERE id = #{studentAssignmentId} AND student_id = #{studentId}")
    StudentAssignment findByIdAndStudentId(@Param("studentAssignmentId") Long studentAssignmentId, @Param("studentId") Long studentId);
}