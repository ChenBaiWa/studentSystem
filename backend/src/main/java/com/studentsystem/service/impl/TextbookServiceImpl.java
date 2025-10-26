package com.studentsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studentsystem.entity.Grade;
import com.studentsystem.entity.SysUser;
import com.studentsystem.entity.Textbook;
import com.studentsystem.mapper.GradeMapper;
import com.studentsystem.mapper.SysUserMapper;
import com.studentsystem.mapper.TextbookMapper;
import com.studentsystem.service.TextbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TextbookServiceImpl implements TextbookService {

    private final TextbookMapper textbookMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private GradeMapper gradeMapper;

    @Override
    public List<Textbook> getTextbooksByGradeIdAndCreatorId(Long gradeId, Long creatorId) {
        return textbookMapper.selectByTextbookList(gradeId, creatorId);
    }

    @Override
    public Textbook createTextbook(Textbook textbook) {
        // 检查同一创建人在同一_grade下是否存在相同名称的课本
        int count = textbookMapper.countByNameAndGradeIdAndCreatorId(
                textbook.getName(), textbook.getGradeId(), textbook.getCreatorId());
        if (count > 0) {
            throw new RuntimeException("该年级下已存在此课本");
        }

        // 设置创建人姓名
        if (textbook.getCreatorId() != null) {
            SysUser creator = sysUserMapper.selectById(Math.toIntExact(textbook.getCreatorId()));
            if (creator != null) {
                textbook.setCreatorName(creator.getRealName());
            }
        }

        // 设置年级名称
        if (textbook.getGradeId() != null) {
            Grade grade = gradeMapper.selectById(textbook.getGradeId());
            if (grade != null) {
                textbook.setGradeName(grade.getName());
            }
        }

        textbook.setCreateTime(LocalDateTime.now());
        textbook.setUpdateTime(LocalDateTime.now());
        textbookMapper.insert(textbook);
        return textbook;
    }

    @Override
    public Textbook updateTextbook(Long id, Textbook textbook) {
        Textbook existingTextbook = getTextbookById(id);

        // 如果修改了名称，需要检查是否重复
        if (!existingTextbook.getName().equals(textbook.getName()) ||
            !existingTextbook.getGradeId().equals(textbook.getGradeId())) {
            int count = textbookMapper.countByNameAndGradeIdAndCreatorId(
                    textbook.getName(), textbook.getGradeId(), textbook.getCreatorId());
            if (count > 0) {
                throw new RuntimeException("该年级下已存在此课本");
            }
        }

        existingTextbook.setName(textbook.getName());
        existingTextbook.setGradeId(textbook.getGradeId());
        existingTextbook.setGradeName(textbook.getGradeName());
        existingTextbook.setUpdateTime(LocalDateTime.now());

        // 更新创建人姓名
        if (textbook.getCreatorId() != null) {
            SysUser creator = sysUserMapper.selectById(Math.toIntExact(textbook.getCreatorId()));
            if (creator != null) {
                existingTextbook.setCreatorName(creator.getRealName());
            }
        }

        // 更新年级名称
        if (textbook.getGradeId() != null) {
            Grade grade = gradeMapper.selectById(textbook.getGradeId());
            if (grade != null) {
                existingTextbook.setGradeName(grade.getName());
            }
        }

        textbookMapper.updateById(existingTextbook);
        return existingTextbook;
    }

    @Override
    public boolean deleteTextbook(Long id) {
        // 检查是否关联章节
        int chapterCount = textbookMapper.countChaptersByTextbookId(id);
        if (chapterCount > 0) {
            throw new RuntimeException("课本已关联章节，无法删除");
        }

        textbookMapper.deleteById(id);
        return true;
    }

    @Override
    public Textbook getTextbookById(Long id) {
        Textbook textbook = textbookMapper.selectById(id);
        if (textbook == null) {
            throw new RuntimeException("课本不存在");
        }
        return textbook;
    }
}
