package com.studentsystem.mapper;

import com.studentsystem.entity.ExerciseSet;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ExerciseSetMapper {

    @Insert("INSERT INTO exercise_set(name, subject, creator_id, creator_name, status, question_count, create_time, update_time) " +
            "VALUES(#{name}, #{subject}, #{creatorId}, #{creatorName}, #{status}, #{questionCount}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExerciseSet exerciseSet);

    @Select("SELECT * FROM exercise_set WHERE id = #{id}")
    ExerciseSet findById(Long id);

    @Select("SELECT * FROM exercise_set WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<ExerciseSet> findByCreatorId(Long creatorId);

    @Update("UPDATE exercise_set SET name = #{name}, subject = #{subject}, update_time = #{updateTime} WHERE id = #{id}")
    int update(ExerciseSet exerciseSet);

    @Update("UPDATE exercise_set SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updateTime") LocalDateTime updateTime);

    @Delete("DELETE FROM exercise_set WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM question WHERE exercise_set_id = #{exerciseSetId}")
    int countQuestionsByExerciseSetId(Long exerciseSetId);
}
