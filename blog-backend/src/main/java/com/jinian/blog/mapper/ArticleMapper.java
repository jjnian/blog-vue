package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.entity.Article;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章Mapper
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("""
            SELECT a.*, c.name as category_name, u.nickname as author_name
            FROM articles a
            LEFT JOIN categories c ON a.category_id = c.id
            LEFT JOIN users u ON a.author_id = u.id
            WHERE a.deleted = 0
            ORDER BY a.is_top DESC, a.created_at DESC
            """)
    IPage<Article> selectPageWithCategory(Page<Article> page);

    @Select("""
            SELECT DISTINCT EXTRACT(YEAR FROM created_at) as year,
                   EXTRACT(MONTH FROM created_at) as month
            FROM articles
            WHERE deleted = 0 AND status = 'PUBLISHED'
            ORDER BY year DESC, month DESC
            """)
    List<Object[]> selectArchives();

    @Select("SELECT * FROM articles WHERE slug = #{slug} AND deleted = 0")
    Article selectBySlug(@Param("slug") String slug);

    @Delete("DELETE FROM article_tags WHERE article_id = #{articleId}")
    int deleteArticleTags(@Param("articleId") Long articleId);

    @Insert("INSERT INTO article_tags (article_id, tag_id) VALUES (#{articleId}, #{tagId})")
    int insertArticleTag(@Param("articleId") Long articleId, @Param("tagId") Long tagId);
}