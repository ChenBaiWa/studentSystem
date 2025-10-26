package com.studentsystem.service;

import com.studentsystem.entity.Assignment;
import com.studentsystem.entity.StudentAssignment;
import com.studentsystem.service.impl.StudentAssignmentListResult;

import java.util.List;

public interface AssignmentService {

    /**
     * 发布作业
     * @param assignment 作业信息
     * @param classIds 班级ID列表
     * @return 作业ID列表
     */
    List<Long> publishAssignment(Assignment assignment, List<Long> classIds);

    /**
     * 获取老师创建的作业列表
     * @param teacherId 老师ID
     * @param classId 班级ID（可选）
     * @param status 状态筛选（可选）
     * @return 作业列表
     */
    List<Assignment> getAssignmentsByTeacher(Long teacherId, Long classId, String status);

    /**
     * 根据ID获取作业详情
     * @param assignmentId 作业ID
     * @return 作业详情
     */
    Assignment getAssignmentById(Long assignmentId);

    /**
     * 更新作业信息
     * @param assignment 作业信息
     * @return 是否更新成功
     */
    boolean updateAssignment(Assignment assignment);

    /**
     * 删除作业
     * @param assignmentId 作业ID
     * @return 是否删除成功
     */
    boolean deleteAssignment(Long assignmentId);

    /**
     * 获取作业的学生提交列表
     * @param assignmentId 作业ID
     * @return 学生提交列表
     */
    List<StudentAssignment> getStudentSubmissions(Long assignmentId);

    /**
     * 获取学生作业列表
     * @param studentId 学生ID
     * @return 学生作业列表
     */
    StudentAssignmentListResult getStudentAssignments(Long studentId);

    /**
     * 学生提交作业
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @param answer 答案（图片URL列表）
     * @return 是否提交成功
     */
    boolean submitAssignment(Long assignmentId, Long studentId, String answer);

    /**
     * 获取学生作业详情
     * @param studentAssignmentId 学生作业ID
     * @param studentId 学生ID
     * @return 学生作业详情
     */
    StudentAssignment getStudentAssignmentDetail(Long studentAssignmentId, Long studentId);
}
