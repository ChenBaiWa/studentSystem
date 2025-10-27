package com.studentsystem.mapper;

import com.studentsystem.entity.Assignment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AssignmentMapper {

    @Insert("INSERT INTO assignment(title, class_id, class_name, chapter_id, chapter_name, content, total_score, deadline, creator_id, creator_name, create_time, update_time) " +
            "VALUES(#{title}, #{classId}, #{className}, #{chapterId}, #{chapterName}, #{content}, #{totalScore}, #{deadline}, #{creatorId}, #{creatorName}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Assignment assignment);

    @Select("SELECT * FROM assignment WHERE id = #{id}")
    Assignment findById(Long id);

    @Select("SELECT * FROM assignment WHERE class_id = #{classId} ORDER BY create_time DESC")
    List<Assignment> findByClassId(Long classId);

    @Select("SELECT * FROM assignment ORDER BY create_time DESC")
    List<Assignment> findAll();

    @Update("UPDATE assignment SET title = #{title}, content = #{content}, total_score = #{totalScore}, deadline = #{deadline}, update_time = #{updateTime} WHERE id = #{id}")
    int update(Assignment assignment);

    @Delete("DELETE FROM assignment WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据班级ID列表查询作业
     * @param classIds 班级ID列表
     * @return 作业列表
     */
    @Select("<script>" +
            "SELECT * FROM assignment WHERE class_id IN " +
            "<foreach item='item' collection='classIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            " ORDER BY create_time DESC" +
            "</script>")
    List<Assignment> selectByClassIds(@Param("classIds") List<Long> classIds);

    /**
     * 根据教师ID和班级ID查询作业
     * @param teacherId 教师ID
     * @param classId 班级ID
     * @return 作业列表
     */
    @Select("SELECT * FROM assignment WHERE creator_id = #{teacherId} AND class_id = #{classId} ORDER BY create_time DESC")
    List<Assignment> selectByTeacherIdAndClassId(@Param("teacherId") Long teacherId, @Param("classId") Long classId);

    /**
     * 根据教师ID查询作业
     * @param teacherId 教师ID
     * @return 作业列表
     */
    @Select("SELECT * FROM assignment WHERE creator_id = #{teacherId} ORDER BY create_time DESC")
    List<Assignment> selectByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 统计指定作业的提交数量
     * @param assignmentId 作业ID
     * @return 提交数量
     */
    @Select("SELECT COUNT(*) FROM student_assignment WHERE assignment_id = #{assignmentId}")
    int countSubmissionsByAssignmentId(@Param("assignmentId") Long assignmentId);
}
