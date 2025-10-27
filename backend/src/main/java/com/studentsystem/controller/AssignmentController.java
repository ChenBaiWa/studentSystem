package com.studentsystem.controller;

import com.studentsystem.entity.Assignment;
import com.studentsystem.entity.Student;
import com.studentsystem.entity.StudentAssignment;
import com.studentsystem.entity.StudentClass;
import com.studentsystem.entity.SysUser;
import com.studentsystem.mapper.StudentClassMapper;
import com.studentsystem.mapper.StudentMapper;
import com.studentsystem.service.AssignmentService;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/assignments")
@CrossOrigin
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private StudentClassMapper studentClassMapper;
    
    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取作业列表
     * @param classId 班级ID（可选）
     * @param status 状态筛选（可选）
     * @param request HTTP请求
     * @return 作业列表
     */
    @GetMapping
    public ResponseEntity<List<Assignment>> getAssignments(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            // 尝试从请求参数中获取用户ID（用于调试）
            String userIdParam = request.getParameter("userId");
            if (userIdParam != null) {
                try {
                    teacherId = Long.parseLong(userIdParam);
                } catch (NumberFormatException e) {
                    // 忽略异常，teacherId仍为null
                }
            }

            if (teacherId == null) {
                return ResponseEntity.status(401).build();
            }
        }

        List<Assignment> assignments = assignmentService.getAssignmentsByTeacher(teacherId, classId, status);


        return ResponseEntity.ok(assignments);
    }

    /**
     * 发布作业
     * @param assignment 作业信息（包含classIds）
     * @param request HTTP请求
     * @return 作业ID列表
     */
    @PostMapping("/publish")
    public ResponseEntity<List<Long>> publishAssignment(
            @RequestBody Assignment assignment,
            HttpServletRequest request) {

        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            return ResponseEntity.status(401).build();
        }

        // 设置创建者信息
        assignment.setCreatorId(teacherId);
        assignment.setCreateTime(LocalDateTime.now());
        assignment.setUpdateTime(LocalDateTime.now());
        
        // 从作业对象中提取classIds
        List<Long> classIds = assignment.getClassIds();

        // 清除classIds字段，避免在数据库操作中出现问题
        assignment.setClassIds(null);

        List<Long> assignmentIds = assignmentService.publishAssignment(assignment, classIds);
        return ResponseEntity.ok(assignmentIds);
    }

    /**
     * 获取作业详情
     * @param id 作业ID
     * @return 作业详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Assignment> getAssignment(@PathVariable Long id) {
        Assignment assignment = assignmentService.getAssignmentById(id);
        if (assignment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(assignment);
    }

    /**
     * 更新作业
     * @param id 作业ID
     * @param assignment 作业信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateAssignment(
            @PathVariable Long id,
            @RequestBody Assignment assignment,
            HttpServletRequest request) {

        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            return ResponseEntity.status(401).build();
        }

        Assignment existingAssignment = assignmentService.getAssignmentById(id);
        if (existingAssignment == null) {
            return ResponseEntity.notFound().build();
        }

        // 设置不可修改的字段
        assignment.setId(id);
        assignment.setClassId(existingAssignment.getClassId());
        assignment.setClassName(existingAssignment.getClassName());
        assignment.setChapterId(existingAssignment.getChapterId());
        assignment.setChapterName(existingAssignment.getChapterName());
        assignment.setCreatorId(existingAssignment.getCreatorId());
        assignment.setCreatorName(existingAssignment.getCreatorName());
        assignment.setCreateTime(existingAssignment.getCreateTime());

        boolean success = assignmentService.updateAssignment(assignment);
        if (success) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.status(500).body("更新失败");
        }
    }

    /**
     * 删除作业
     * @param id 作业ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAssignment(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long teacherId = jwtUtil.getUserIdFromRequest(request);
        if (teacherId == null) {
            return ResponseEntity.status(401).build();
        }

        Assignment existingAssignment = assignmentService.getAssignmentById(id);
        if (existingAssignment == null) {
            return ResponseEntity.notFound().build();
        }

        boolean success = assignmentService.deleteAssignment(id);
        if (success) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.status(400).body("已有学生提交，无法删除");
        }
    }

    /**
     * 获取作业的学生提交列表（包含未提交的学生）
     * @param id 作业ID
     * @return 学生提交列表
     */
    @GetMapping("/{id}/submissions")
    public ResponseEntity<Map<String, Object>> getStudentSubmissions(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Assignment assignment = assignmentService.getAssignmentById(id);
            if (assignment == null) {
                response.put("success", false);
                response.put("message", "作业不存在");
                return ResponseEntity.notFound().build();
            }

            // 获取该班级的所有学生
            List<StudentClass> studentClasses = studentClassMapper.selectByClassId(assignment.getClassId());
            List<Long> studentIds = studentClasses.stream()
                    .map(StudentClass::getStudentId)
                    .collect(Collectors.toList());
            
            List<Student> allStudents = new ArrayList<>();
            if (!studentIds.isEmpty()) {
                allStudents = studentMapper.selectByIds(studentIds);
            }

            // 获取已提交的学生作业
            List<StudentAssignment> submittedAssignments = assignmentService.getStudentSubmissions(id);
            
            // 构建完整的学生提交情况列表
            List<Object> result = new ArrayList<>();
            for (Student student : allStudents) {
                // 查找该学生是否已提交作业
                StudentAssignment submission = submittedAssignments.stream()
                        .filter(sa -> sa.getStudentId().equals(student.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (submission != null) {
                    // 已提交
                    result.add(submission);
                } else {
                    // 未提交，创建一个表示未提交的记录
                    StudentAssignment notSubmitted = new StudentAssignment();
                    notSubmitted.setStudentId(student.getId());
                    notSubmitted.setStudentName(student.getName());
                    notSubmitted.setAssignmentId(id);
                    notSubmitted.setStatus("not_submitted");
                    result.add(notSubmitted);
                }
            }
            
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取学生提交列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}