package com.studentsystem.mapper;

import com.studentsystem.entity.Chapter;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChapterMapper {
    
    /**
     * 根据课本ID查询章节列表
     * @param textbookId 课本ID
     * @return 章节列表
     */
    @Select("SELECT * FROM chapter WHERE textbook_id = #{textbookId}")
    List<Chapter> findByTextbookId(Long textbookId);
    
    /**
     * 根据ID查询章节
     * @param id 章节ID
     * @return 章节信息
     */
    @Select("SELECT * FROM chapter WHERE id = #{id}")
    Chapter findById(Long id);
    
    /**
     * 插入新章节
     * @param chapter 章节信息
     * @return 影响行数
     */
    @Insert("INSERT INTO chapter(name, textbook_id, textbook_name, creator_id, creator_name) " +
            "VALUES(#{name}, #{textbookId}, #{textbookName}, #{creatorId}, #{creatorName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Chapter chapter);
    
    /**
     * 更新章节信息
     * @param chapter 章节信息
     * @return 影响行数
     */
    @Update("UPDATE chapter SET name = #{name}, update_time = NOW() WHERE id = #{id}")
    int update(Chapter chapter);
    
    /**
     * 删除章节
     * @param id 章节ID
     * @return 影响行数
     */
    @Delete("DELETE FROM chapter WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 根据课本ID删除章节
     * @param textbookId 课本ID
     * @return 影响行数
     */
    @Delete("DELETE FROM chapter WHERE textbook_id = #{textbookId}")
    int deleteByTextbookId(Long textbookId);
    
    /**
     * 根据创建人ID和章节名称、课本ID检查是否重复
     * @param creatorId 创建人ID
     * @param name 章节名称
     * @param textbookId 课本ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM chapter WHERE creator_id = #{creatorId} AND name = #{name} AND textbook_id = #{textbookId}")
    boolean existsByNameAndTextbookIdAndCreatorId(@Param("creatorId") Long creatorId, @Param("name") String name, @Param("textbookId") Long textbookId);
}