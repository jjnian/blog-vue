package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinian.blog.entity.Tag;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签Mapper
 */
public interface TagMapper extends BaseMapper<Tag> {

    @Select("""
            SELECT t.* FROM tags t
            INNER JOIN article_tags at ON t.id = at.tag_id
            WHERE at.article_id = #{articleId} AND t.deleted = 0
            """)
    List<Tag> selectByArticleId(@Param("articleId") Long articleId);
}