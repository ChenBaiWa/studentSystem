package com.studentsystem.service.impl;

import com.studentsystem.entity.Assignment;
import com.studentsystem.entity.Chapter;
import com.studentsystem.entity.Class;
import com.studentsystem.entity.Student;
import com.studentsystem.entity.StudentAssignment;
import com.studentsystem.mapper.AssignmentMapper;
import com.studentsystem.mapper.ChapterMapper;
import com.studentsystem.mapper.ClassMapper;
import com.studentsystem.mapper.StudentAssignmentMapper;
import com.studentsystem.mapper.StudentMapper;
import com.studentsystem.service.AssignmentService;
import com.studentsystem.service.impl.StudentAssignmentListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private StudentAssignmentMapper studentAssignmentMapper;
    @Autowired
    private GradeServiceImpl gradeService;
    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<Long> publishAssignment(Assignment assignment, List<Long> classIds) {
        List<Long> assignmentIds = new ArrayList<>();

        // 为每个班级创建一份作业
        for (Long classId : classIds) {
            Assignment newAssignment = new Assignment();
            newAssignment.setTitle(assignment.getTitle());
            newAssignment.setClassId(classId);

            // 获取班级名称
            Class clazz = classMapper.findById(classId);
            if (clazz != null) {
                newAssignment.setClassName(clazz.getName());
            }

            // 设置章节信息
            newAssignment.setChapterId(assignment.getChapterId());
            newAssignment.setChapterName(assignment.getChapterName());

            // 如果章节名称为空，尝试从数据库获取
            if (assignment.getChapterName() == null && assignment.getChapterId() != null) {
                Chapter chapter = chapterMapper.findById(assignment.getChapterId());
                if (chapter != null) {
                    newAssignment.setChapterName(chapter.getName());
                }
            }

            newAssignment.setContent(assignment.getContent());
            newAssignment.setTotalScore(assignment.getTotalScore());
            newAssignment.setDeadline(assignment.getDeadline());
            newAssignment.setCreatorId(assignment.getCreatorId());
            newAssignment.setCreatorName(assignment.getCreatorName());
            newAssignment.setCreateTime(LocalDateTime.now());
            newAssignment.setUpdateTime(LocalDateTime.now());

            assignmentMapper.insert(newAssignment);
            assignmentIds.add(newAssignment.getId());
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
            assignment.setSubmissionCount(submissionCount);
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
        // 物理删除作业
        return assignmentMapper.deleteById(assignmentId) > 0;
    }

    @Override
    public List<StudentAssignment> getStudentSubmissions(Long assignmentId) {
        return studentAssignmentMapper.findByAssignmentId(assignmentId);
    }

    @Override
    public StudentAssignmentListResult getStudentAssignments(Long studentId) {
        // 获取学生提交的作业
        List<StudentAssignment> submittedAssignments = studentAssignmentMapper.findByStudentId(studentId);

        // 创建结果对象
        StudentAssignmentListResult result = new StudentAssignmentListResult();
        result.setSubmitted(submittedAssignments);

        // 初始化其他列表
        result.setPending(new ArrayList<>());
        result.setExpired(new ArrayList<>());

        return result;
    }

    @Override
    public boolean submitAssignment(Long assignmentId, Long studentId, String answer) {
        // 获取学生信息
        Student student = studentMapper.selectByUserId(studentId);

        // 创建学生作业对象
        StudentAssignment studentAssignment = new StudentAssignment();
        studentAssignment.setAssignmentId(assignmentId);
        studentAssignment.setStudentId(studentId);
        studentAssignment.setStudentName(student != null ? student.getName() : "未知学生");
        studentAssignment.setAnswer(answer);
        studentAssignment.setStatus("submitted");
        studentAssignment.setSubmitTime(LocalDateTime.now());
        studentAssignment.setCreateTime(LocalDateTime.now());
        studentAssignment.setUpdateTime(LocalDateTime.now());

        // 插入到数据库
        return studentAssignmentMapper.insert(studentAssignment) > 0;
    }

    @Override
    public StudentAssignment getStudentAssignmentDetail(Long studentAssignmentId, Long studentId) {
        return studentAssignmentMapper.findByIdAndStudentId(studentAssignmentId, studentId);
    }
}
