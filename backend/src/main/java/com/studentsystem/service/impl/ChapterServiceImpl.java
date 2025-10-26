package com.studentsystem.service.impl;

import com.studentsystem.entity.Chapter;
import com.studentsystem.entity.SysUser;
import com.studentsystem.entity.Textbook;
import com.studentsystem.mapper.ChapterMapper;
import com.studentsystem.mapper.SysUserMapper;
import com.studentsystem.mapper.TextbookMapper;
import com.studentsystem.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private TextbookMapper textbookMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<Chapter> findByTextbookId(Long textbookId) {
        return chapterMapper.findByTextbookId(textbookId);
    }

    @Override
    public Chapter createChapter(Chapter chapter) {
        // 校验章节名称是否重复
        if (chapterMapper.existsByNameAndTextbookIdAndCreatorId(
                chapter.getCreatorId(), chapter.getName(), chapter.getTextbookId())) {
            throw new RuntimeException("该课本下已存在同名章节");
        }

        // 设置课本名称
        Textbook textbook = textbookMapper.selectById(chapter.getTextbookId());
        if (textbook != null) {
            chapter.setTextbookName(textbook.getName());
        }

        // 设置创建人姓名
        SysUser creator = sysUserMapper.selectById(Math.toIntExact(chapter.getCreatorId()));
        if (creator != null) {
            chapter.setCreatorName(creator.getRealName());
        }

        // 设置创建和更新时间
        chapter.setCreateTime(LocalDateTime.now());
        chapter.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        chapterMapper.insert(chapter);
        return chapter;
    }

    @Override
    public Chapter updateChapter(Chapter chapter) {
        // 校验章节是否存在
        Chapter existingChapter = chapterMapper.findById(chapter.getId());
        if (existingChapter == null) {
            throw new RuntimeException("章节不存在");
        }

        // 校验章节名称是否重复（排除自己）
        if (!existingChapter.getName().equals(chapter.getName()) &&
            chapterMapper.existsByNameAndTextbookIdAndCreatorId(
                chapter.getCreatorId(), chapter.getName(), chapter.getTextbookId())) {
            throw new RuntimeException("该课本下已存在同名章节");
        }

        // 更新信息
        existingChapter.setName(chapter.getName());
        existingChapter.setUpdateTime(LocalDateTime.now());

        chapterMapper.update(existingChapter);
        return existingChapter;
    }

    @Override
    public void deleteChapter(Long id) {
        chapterMapper.deleteById(id);
    }

    @Override
    public Chapter findById(Long id) {
        return chapterMapper.findById(id);
    }
}
