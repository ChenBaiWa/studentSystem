package com.studentsystem.service.impl;

import com.studentsystem.entity.Assignment;
import com.studentsystem.entity.Class;
import com.studentsystem.entity.StudentAssignment;
import com.studentsystem.entity.SysUser;
import com.studentsystem.mapper.*;
import com.studentsystem.service.AssignmentService;
import com.studentsystem.service.impl.StudentAssignmentListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private StudentAssignmentMapper studentAssignmentMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private StudentClassMapper studentClassMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public List<Long> publishAssignment(Assignment assignment, List<Long> classIds) {
        List<Long> assignmentIds = new ArrayList<>();

        // 获取创建者信息
        SysUser creator = sysUserMapper.selectById(Math.toIntExact(assignment.getCreatorId()));
        String creatorName = creator != null ? creator.getRealName() : "";

        // 获取班级信息
        for (Long classId : classIds) {
            Class clazz = classMapper.findById(classId);
            if (clazz != null) {
                // 复制作业对象
                Assignment newAssignment = new Assignment();
                newAssignment.setTitle(assignment.getTitle());
                newAssignment.setClassId(classId);
                newAssignment.setClassName(clazz.getName());
                newAssignment.setChapterId(assignment.getChapterId());
                newAssignment.setChapterName(chapterMapper.findById(assignment.getChapterId()).getName());
                newAssignment.setContent(assignment.getContent());
                newAssignment.setTotalScore(assignment.getTotalScore());
                newAssignment.setDeadline(assignment.getDeadline());
                newAssignment.setCreatorId(assignment.getCreatorId());
                newAssignment.setCreatorName(creatorName);
                newAssignment.setCreateTime(LocalDateTime.now());
                newAssignment.setUpdateTime(LocalDateTime.now());

                // 插入作业记录
                assignmentMapper.insert(newAssignment);
                assignmentIds.add(newAssignment.getId());
            }
        }

        return assignmentIds;
    }

    @Override
    public List<Assignment> getAssignmentsByTeacher(Long teacherId, Long classId, String status) {
        List<Assignment> assignments;
        if (classId != null && classId > 0) {
            assignments = assignmentMapper.selectByTeacherIdAndClassId(teacherId, classId);
        } else {
            assignments = assignmentMapper.selectByTeacherId(teacherId);
        }

        // 补充统计信息（提交人数等）
        for (Assignment assignment : assignments) {
            int submissionCount = assignmentMapper.countSubmissionsByAssignmentId(assignment.getId());
            // 这里可以补充班级总人数等信息
            // 由于需要额外查询，暂时只设置提交人数
        }

        return assignments;
    }

    @Override
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentMapper.findById(assignmentId);
    }

    @Override
    public boolean updateAssignment(Assignment assignment) {
        assignment.setUpdateTime(LocalDateTime.now());
        return assignmentMapper.update(assignment) > 0;
    }

    @Override
    public boolean deleteAssignment(Long assignmentId) {
        // 检查是否有学生提交
        int submissionCount = assignmentMapper.countSubmissionsByAssignmentId(assignmentId);
        if (submissionCount > 0) {
            return false; // 有学生提交，不能删除
        }
        return assignmentMapper.deleteById(assignmentId) > 0;
    }

    @Override
    public List<StudentAssignment> getStudentSubmissions(Long assignmentId) {
        return studentAssignmentMapper.selectByAssignmentId(assignmentId);
    }

    @Override
    public StudentAssignmentListResult getStudentAssignments(Long studentId) {
        // 获取学生加入的所有班级
        List<Class> classes = classMapper.findJoinedClassesByStudentId(studentId);
        List<Long> classIds = classes.stream().map(Class::getId).collect(Collectors.toList());
        
        if (classIds.isEmpty()) {
            // 学生未加入任何班级
            return new StudentAssignmentListResult();
        }
        
        // 获取这些班级的所有作业
        List<Assignment> assignments = assignmentMapper.selectByClassIds(classIds);
        
        // 获取学生已提交的作业
        List<StudentAssignment> submittedAssignments = studentAssignmentMapper.selectByStudentId(studentId);
        Map<Long, StudentAssignment> submittedMap = submittedAssignments.stream()
                .collect(Collectors.toMap(StudentAssignment::getAssignmentId, sa -> sa));
        
        // 分类作业
        List<Assignment> pending = new ArrayList<>();
        List<StudentAssignment> submitted = new ArrayList<>();
        List<Assignment> expired = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        for (Assignment assignment : assignments) {
            if (submittedMap.containsKey(assignment.getId())) {
                // 已提交
                submitted.add(submittedMap.get(assignment.getId()));
            } else if (assignment.getDeadline().isBefore(now)) {
                // 已截止
                expired.add(assignment);
            } else {
                // 待提交
                pending.add(assignment);
            }
        }
        
        StudentAssignmentListResult result = new StudentAssignmentListResult();
        result.setPending(pending);
        result.setSubmitted(submitted);
        result.setExpired(expired);
        
        return result;
    }

    @Override
    public boolean submitAssignment(Long assignmentId, Long studentId, String answer) {
        // 检查作业是否存在
        Assignment assignment = assignmentMapper.findById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }
        
        // 检查学生是否已加入该作业所属的班级
        if (!studentClassMapper.existsByStudentIdAndClassId(studentId, assignment.getClassId())) {
            throw new RuntimeException("您未加入该作业所属的班级");
        }
        
        // 检查是否已提交过该作业
        StudentAssignment existing = studentAssignmentMapper.selectByAssignmentIdAndStudentId(assignmentId, studentId);
        if (existing != null) {
            throw new RuntimeException("您已提交过该作业");
        }
        
        // 检查是否已过截止时间
        if (assignment.getDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("作业已截止，无法提交");
        }
        
        // 创建学生作业提交记录
        StudentAssignment studentAssignment = new StudentAssignment();
        studentAssignment.setAssignmentId(assignmentId);
        studentAssignment.setStudentId(studentId);
        
        // 获取学生姓名
        SysUser student = sysUserMapper.selectById(studentId.intValue());
        studentAssignment.setStudentName(student != null ? student.getRealName() : "");
        
        studentAssignment.setAnswer(answer);
        studentAssignment.setStatus("submitted");
        studentAssignment.setSubmitTime(LocalDateTime.now());
        studentAssignment.setCreateTime(LocalDateTime.now());
        studentAssignment.setUpdateTime(LocalDateTime.now());
        
        return studentAssignmentMapper.insert(studentAssignment) > 0;
    }

    @Override
    public StudentAssignment getStudentAssignmentDetail(Long studentAssignmentId, Long studentId) {
        // TODO: 实现获取学生作业详情的逻辑
        return null;
    }
}