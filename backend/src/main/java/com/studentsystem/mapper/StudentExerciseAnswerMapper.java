package com.studentsystem.mapper;

import com.studentsystem.entity.StudentExerciseAnswer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentExerciseAnswerMapper {
    
    @Insert("INSERT INTO student_exercise_answer(student_id, question_id, answer, answer_type, image_urls, create_time, update_time) " +
            "VALUES(#{studentId}, #{questionId}, #{answer}, #{answerType}, #{imageUrls}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentExerciseAnswer answer);
    
    @Select("SELECT * FROM student_exercise_answer WHERE id = #{id}")
    StudentExerciseAnswer findById(Long id);
    
    @Select("SELECT * FROM student_exercise_answer WHERE student_id = #{studentId} AND question_id = #{questionId}")
    StudentExerciseAnswer findByStudentIdAndQuestionId(@Param("studentId") Long studentId, @Param("questionId") Long questionId);
    
    @Select("SELECT * FROM student_exercise_answer WHERE student_id = #{studentId} AND question_id IN (${questionIds})")
    List<StudentExerciseAnswer> findByStudentIdAndQuestionIds(@Param("studentId") Long studentId, @Param("questionIds") String questionIds);
    
    @Update("UPDATE student_exercise_answer SET answer = #{answer}, answer_type = #{answerType}, image_urls = #{imageUrls}, update_time = #{updateTime} WHERE id = #{id}")
    int update(StudentExerciseAnswer answer);
    
    @Delete("DELETE FROM student_exercise_answer WHERE id = #{id}")
    int deleteById(Long id);
    
    @Delete("DELETE FROM student_exercise_answer WHERE student_id = #{studentId} AND question_id IN (${questionIds})")
    int deleteByStudentIdAndQuestionIds(@Param("studentId") Long studentId, @Param("questionIds") String questionIds);
}