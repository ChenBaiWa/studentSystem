package com.studentsystem.mapper;

import com.studentsystem.entity.Class;
import com.studentsystem.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClassMapper {
    
    /**
     * 根据创建人ID查询班级列表
     * @param creatorId 创建人ID(老师ID)
     * @return 班级列表
     */
    @Select("SELECT * FROM class WHERE creator_id = #{creatorId}")
    List<Class> findByCreatorId(Long creatorId);
    
    /**
     * 根据ID查询班级
     * @param id 班级ID
     * @return 班级信息
     */
    @Select("SELECT * FROM class WHERE id = #{id}")
    Class findById(Long id);
    
    /**
     * 根据班级号查询班级
     * @param classCode 班级号
     * @return 班级信息
     */
    @Select("SELECT * FROM class WHERE class_code = #{classCode}")
    Class findByClassCode(String classCode);
    
    /**
     * 插入新班级
     * @param clazz 班级信息
     * @return 影响行数
     */
    @Insert("INSERT INTO class(name, class_code, verification_code, grade_id, grade_name, creator_id, creator_name) " +
            "VALUES(#{name}, #{classCode}, #{verificationCode}, #{gradeId}, #{gradeName}, #{creatorId}, #{creatorName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Class clazz);
    
    /**
     * 更新班级信息
     * @param clazz 班级信息
     * @return 影响行数
     */
    @Update("UPDATE class SET name = #{name}, grade_id = #{gradeId}, grade_name = #{gradeName}, update_time = NOW() WHERE id = #{id}")
    int update(Class clazz);
    
    /**
     * 删除班级
     * @param id 班级ID
     * @return 影响行数
     */
    @Delete("DELETE FROM class WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 根据班级号检查是否存在
     * @param classCode 班级号
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM class WHERE class_code = #{classCode}")
    boolean existsByClassCode(String classCode);
    
    /**
     * 根据创建人ID和班级名称检查是否重复
     * @param creatorId 创建人ID
     * @param name 班级名称
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM class WHERE creator_id = #{creatorId} AND name = #{name}")
    boolean existsByNameAndCreatorId(@Param("creatorId") Long creatorId, @Param("name") String name);
    
    /**
     * 查询班级下的学生列表
     * @param classId 班级ID
     * @return 学生列表
     */
    @Select("SELECT s.* FROM student s JOIN student_class sc ON s.id = sc.student_id WHERE sc.class_id = #{classId}")
    List<Student> findStudentsByClassId(Long classId);
    
    /**
     * 根据学生ID查询已加入的班级列表
     * @param studentId 学生ID
     * @return 班级列表
     */
    @Select("SELECT c.* FROM class c JOIN student_class sc ON c.id = sc.class_id WHERE sc.student_id = #{studentId}")
    List<Class> findJoinedClassesByStudentId(Long studentId);
}