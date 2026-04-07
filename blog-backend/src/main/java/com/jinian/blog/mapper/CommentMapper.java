package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinian.blog.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论Mapper
 */
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT * FROM comments WHERE article_id = #{articleId} AND deleted = 0 AND status = 'APPROVED' ORDER BY created_at DESC")
    List<Comment> selectByArticleId(@Param("articleId") Long articleId);
}