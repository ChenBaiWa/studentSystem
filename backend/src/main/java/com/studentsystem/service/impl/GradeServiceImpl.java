package com.studentsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studentsystem.entity.Grade;
import com.studentsystem.entity.SysUser;
import com.studentsystem.mapper.GradeMapper;
import com.studentsystem.mapper.SysUserMapper;
import com.studentsystem.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeMapper gradeMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<Grade> getGradesByCreatorId(Long creatorId) {
        return gradeMapper.selectByCreatorId(creatorId);
    }

    @Override
    public Grade createGrade(Grade grade) {
        // 设置创建人姓名
        if (grade.getCreatorId() != null) {
            SysUser creator = sysUserMapper.selectById(Math.toIntExact(grade.getCreatorId()));
            if (creator != null) {
                grade.setCreatorName(creator.getRealName());
            }
        }

        grade.setCreateTime(LocalDateTime.now());
        grade.setUpdateTime(LocalDateTime.now());
        gradeMapper.insert(grade);
        return grade;
    }

    @Override
    public Grade updateGrade(Long id, Grade grade) {
        Grade existingGrade = getGradeById(id);
        existingGrade.setName(grade.getName());
        existingGrade.setUpdateTime(LocalDateTime.now());

        // 更新创建人姓名
        if (grade.getCreatorId() != null) {
            SysUser creator = sysUserMapper.selectById(Math.toIntExact(grade.getCreatorId()));
            if (creator != null) {
                existingGrade.setCreatorName(creator.getRealName());
            }
        }

        gradeMapper.updateById(existingGrade);
        return existingGrade;
    }

    @Override
    public boolean deleteGrade(Long id) {
        gradeMapper.deleteById(id);
        return true;
    }

    @Override
    public Grade getGradeById(Long id) {
        Grade grade = gradeMapper.selectById(id);
        if (grade == null) {
            throw new RuntimeException("年级不存在");
        }
        return grade;
    }
}
