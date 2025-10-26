package com.studentsystem.controller;

import com.studentsystem.entity.Assignment;
import com.studentsystem.entity.StudentAssignment;
import com.studentsystem.mapper.AssignmentMapper;
import com.studentsystem.mapper.StudentAssignmentMapper;
import com.studentsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student-assignments")
@CrossOrigin
public class StudentAssignmentController {

    @Autowired
    private StudentAssignmentMapper studentAssignmentMapper;

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取学生的所有作业提交记录
     * @param request HTTP请求
     * @return 作业提交记录列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStudentAssignments(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            List<StudentAssignment> studentAssignments = studentAssignmentMapper.selectByStudentId(studentId);
            response.put("success", true);
            response.put("data", studentAssignments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取作业提交记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取学生作业提交详情
     * @param id 作业提交ID
     * @param request HTTP请求
     * @return 作业提交详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudentAssignment(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            StudentAssignment studentAssignment = studentAssignmentMapper.findById(id);
            if (studentAssignment == null) {
                response.put("success", false);
                response.put("message", "作业提交记录不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            // 获取作业详情
            Assignment assignment = assignmentMapper.findById(studentAssignment.getAssignmentId());
            if (assignment == null) {
                response.put("success", false);
                response.put("message", "作业不存在");
                return ResponseEntity.status(404).body(response);
            }
            
            // 组合返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("studentAssignment", studentAssignment);
            data.put("assignment", assignment);
            
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取作业详情失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 提交作业
     * @param request HTTP请求
     * @param data 提交数据
     * @return 提交结果
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitAssignment(
            HttpServletRequest request,
            @RequestBody SubmitAssignmentRequest data) {
        Map<String, Object> response = new HashMap<>();
        Long studentId = jwtUtil.getUserIdFromRequest(request);
        if (studentId == null) {
            response.put("success", false);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 检查作业是否存在
            Assignment assignment = assignmentMapper.findById(data.getAssignmentId());
            if (assignment == null) {
                response.put("success", false);
                response.put("message", "作业不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 检查是否已提交过该作业
            StudentAssignment existingSubmission = studentAssignmentMapper.selectByAssignmentIdAndStudentId(
                    data.getAssignmentId(), studentId);
            if (existingSubmission != null) {
                response.put("success", false);
                response.put("message", "作业已提交，不能重复提交");
                return ResponseEntity.status(400).body(response);
            }

            // 检查是否超过截止时间
            if (LocalDateTime.now().isAfter(assignment.getDeadline())) {
                response.put("success", false);
                response.put("message", "已超过作业截止时间，无法提交");
                return ResponseEntity.status(400).body(response);
            }

            // 创建作业提交记录
            StudentAssignment studentAssignment = new StudentAssignment();
            studentAssignment.setAssignmentId(data.getAssignmentId());
            studentAssignment.setStudentId(studentId);
            studentAssignment.setAnswer(data.getAnswer());
            studentAssignment.setStatus("submitted");
            studentAssignment.setSubmitTime(LocalDateTime.now());
            studentAssignment.setCreateTime(LocalDateTime.now());
            studentAssignment.setUpdateTime(LocalDateTime.now());

            studentAssignmentMapper.insert(studentAssignment);

            response.put("success", true);
            response.put("message", "提交成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    // 提交作业请求数据类
    static class SubmitAssignmentRequest {
        private Long assignmentId;
        private String answer; // 图片URL列表，用逗号分隔

        // Getters and setters
        public Long getAssignmentId() {
            return assignmentId;
        }

        public void setAssignmentId(Long assignmentId) {
            this.assignmentId = assignmentId;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}