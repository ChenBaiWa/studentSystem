package com.studentsystem.service;

import com.studentsystem.entity.Class;
import com.studentsystem.entity.Student;
import java.util.List;

public interface ClassService {
    
    /**
     * 根据创建人ID查询班级列表
     * @param creatorId 创建人ID(老师ID)
     * @return 班级列表
     */
    List<Class> findByCreatorId(Long creatorId);
    
    /**
     * 创建班级
     * @param clazz 班级信息
     * @return 创建的班级
     */
    Class createClass(Class clazz);
    
    /**
     * 更新班级信息
     * @param clazz 班级信息
     * @return 更新后的班级信息
     */
    Class updateClass(Class clazz);
    
    /**
     * 删除班级
     * @param id 班级ID
     */
    void deleteClass(Long id);
    
    /**
     * 根据ID查询班级
     * @param id 班级ID
     * @return 班级信息
     */
    Class findById(Long id);
    
    /**
     * 学生加入班级
     * @param classCode 班级号
     * @param verificationCode 验证码
     * @param studentId 学生ID
     * @return 是否加入成功
     */
    boolean joinClass(String classCode, String verificationCode, Long studentId);
    
    /**
     * 根据学生ID查询已加入的班级列表
     * @param studentId 学生ID
     * @return 班级列表
     */
    List<Class> findJoinedClassesByStudentId(Long studentId);
    
    /**
     * 查询班级下的学生列表
     * @param classId 班级ID
     * @return 学生列表
     */
    List<Student> findStudentsByClassId(Long classId);
}