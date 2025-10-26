package com.studentsystem.service.impl;

import com.studentsystem.entity.Class;
import com.studentsystem.entity.Grade;
import com.studentsystem.entity.Student;
import com.studentsystem.entity.StudentClass;
import com.studentsystem.entity.SysUser;
import com.studentsystem.mapper.ClassMapper;
import com.studentsystem.mapper.GradeMapper;
import com.studentsystem.mapper.StudentClassMapper;
import com.studentsystem.mapper.StudentMapper;
import com.studentsystem.mapper.SysUserMapper;
import com.studentsystem.service.ClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class ClassServiceImpl implements ClassService {

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private StudentClassMapper studentClassMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<Class> findByCreatorId(Long creatorId) {
        return classMapper.findByCreatorId(creatorId);
    }

    @Override
    public Class createClass(Class clazz) {
        // 校验班级名称是否重复
        if (classMapper.existsByNameAndCreatorId(clazz.getCreatorId(), clazz.getName())) {
            throw new RuntimeException("同一老师下班级名称不能重复");
        }

        // 设置创建人姓名
        if (clazz.getCreatorId() != null) {
            SysUser creator = sysUserMapper.selectById(Math.toIntExact(clazz.getCreatorId()));
            if (creator != null) {
                clazz.setCreatorName(creator.getRealName());
            }
        }

        // 设置年级名称
        if (clazz.getGradeId() != null) {
            Grade grade = gradeMapper.selectById(clazz.getGradeId());
            if (grade != null) {
                clazz.setGradeName(grade.getName());
            }
        }

        // 生成班级号(6位数字)
        String classCode;
        do {
            classCode = generateRandomNumber(6);
        } while (classMapper.existsByClassCode(classCode));
        clazz.setClassCode(classCode);

        // 生成验证码(4位数字)
        String verificationCode = generateRandomNumber(4);
        clazz.setVerificationCode(verificationCode);

        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        clazz.setCreateTime(now);
        clazz.setUpdateTime(now);

        // 插入数据库
        classMapper.insert(clazz);
        return clazz;
    }

    @Override
    public Class updateClass(Class clazz) {
        // 校验班级名称是否重复(排除自己)
        Class existingClass = classMapper.findById(clazz.getId());
        if (existingClass == null) {
            throw new RuntimeException("班级不存在");
        }

        if (!existingClass.getName().equals(clazz.getName()) &&
            classMapper.existsByNameAndCreatorId(clazz.getCreatorId(), clazz.getName())) {
            throw new RuntimeException("同一老师下班级名称不能重复");
        }

        // 设置创建人姓名
        if (clazz.getCreatorId() != null) {
            SysUser creator = sysUserMapper.selectById(Math.toIntExact(clazz.getCreatorId()));
            if (creator != null) {
                clazz.setCreatorName(creator.getRealName());
            }
        }

        // 设置年级名称
        if (clazz.getGradeId() != null) {
            Grade grade = gradeMapper.selectById(clazz.getGradeId());
            if (grade != null) {
                clazz.setGradeName(grade.getName());
            }
        }

        // 更新时间和更新人信息应由前端传递

        clazz.setUpdateTime(LocalDateTime.now());
        classMapper.update(clazz);
        return clazz;
    }

    @Override
    public void deleteClass(Long id) {
        // 先删除学生-班级关联关系(在完整的实现中)
        // ...

        // 删除班级
        classMapper.deleteById(id);
    }

    @Override
    public Class findById(Long id) {
        return classMapper.findById(id);
    }

    @Override
    public boolean joinClass(String classCode, String verificationCode, Long studentId) {
        // 查找班级
        Class clazz = classMapper.findByClassCode(classCode);
        if (clazz == null) {
            throw new RuntimeException("班级号不存在");
        }

        // 验证验证码
        if (!clazz.getVerificationCode().equals(verificationCode)) {
            throw new RuntimeException("验证码错误");
        }

        // 检查是否已加入该班级
        if (studentClassMapper.existsByStudentIdAndClassId(studentId, clazz.getId())) {
            throw new RuntimeException("已加入该班级");
        }

        // 检查学生表中是否存在该学生记录，如果不存在则创建
        Student student = studentMapper.selectByUserId(studentId);
        if (student == null) {
            // 从sys_user表中获取学生信息
            SysUser sysUser = sysUserMapper.selectById(studentId.intValue());
            if (sysUser != null) {
                student = new Student();
                student.setId(studentId); // 使用用户ID作为学生ID
                student.setName(sysUser.getRealName());
                // 其他字段使用默认值
                student.setGender("未知");
                student.setAge(0);
                student.setCreateTime(LocalDateTime.now());
                student.setUpdateTime(LocalDateTime.now());
                studentMapper.insert(student);
            }
        }

        // 添加学生到班级
        StudentClass studentClass = new StudentClass();
        studentClass.setStudentId(studentId);
        studentClass.setClassId(clazz.getId());
        studentClass.setJoinTime(LocalDateTime.now());
        studentClassMapper.insert(studentClass);

        return true;
    }

    @Override
    public List<Class> findJoinedClassesByStudentId(Long studentId) {
        return classMapper.findJoinedClassesByStudentId(studentId);
    }

    @Override
    public List<Student> findStudentsByClassId(Long classId) {
        return classMapper.findStudentsByClassId(classId);
    }

    /**
     * 生成指定长度的随机数字字符串
     * @param length 长度
     * @return 随机数字字符串
     */
    private String generateRandomNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
