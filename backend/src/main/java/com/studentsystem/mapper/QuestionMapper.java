package com.studentsystem.mapper;

import com.studentsystem.entity.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionMapper {
    
    @Insert("INSERT INTO question(exercise_set_id, type, content, options, answer, score, sort_order, create_time, update_time) " +
            "VALUES(#{exerciseSetId}, #{type}, #{content}, #{options}, #{answer}, #{score}, #{sortOrder}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Question question);
    
    @Select("SELECT * FROM question WHERE id = #{id}")
    Question findById(Long id);
    
    @Select("SELECT * FROM question WHERE exercise_set_id = #{exerciseSetId} ORDER BY sort_order")
    List<Question> findByExerciseSetId(Long exerciseSetId);
    
    @Update("UPDATE question SET type = #{type}, content = #{content}, options = #{options}, answer = #{answer}, score = #{score}, update_time = #{updateTime} WHERE id = #{id}")
    int update(Question question);
    
    @Delete("DELETE FROM question WHERE id = #{id}")
    int deleteById(Long id);
    
    @Delete("DELETE FROM question WHERE exercise_set_id = #{exerciseSetId}")
    int deleteByExerciseSetId(Long exerciseSetId);
    
    /**
     * 根据习题集ID和章节ID获取题目列表
     * @param exerciseSetId 习题集ID
     * @param chapterId 章节ID
     * @return 题目列表
     */
    @Select("SELECT * FROM question WHERE exercise_set_id = #{exerciseSetId} AND chapter_id = #{chapterId} ORDER BY sort_order")
    List<Question> findByExerciseSetIdAndChapterId(@Param("exerciseSetId") Long exerciseSetId, @Param("chapterId") Long chapterId);
}