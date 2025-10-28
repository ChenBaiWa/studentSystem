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
    
    /**
     * 更新批改结果
     */
    @Update("UPDATE student_exercise_answer SET score = #{score}, remark = #{remark}, correct_status = #{correctStatus}, update_time = #{updateTime} WHERE id = #{id}")
    int updateGradingResult(StudentExerciseAnswer answer);
    
    /**
     * 根据学生ID和习题集ID获取答题结果
     */
    @Select("SELECT sea.* FROM student_exercise_answer sea " +
            "JOIN question q ON sea.question_id = q.id " +
            "WHERE sea.student_id = #{studentId} AND q.exercise_set_id = #{exerciseSetId}")
    List<StudentExerciseAnswer> findByStudentIdAndExerciseSetId(@Param("studentId") Long studentId, @Param("exerciseSetId") Long exerciseSetId);
}