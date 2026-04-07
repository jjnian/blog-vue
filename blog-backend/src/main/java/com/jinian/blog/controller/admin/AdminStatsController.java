package com.jinian.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jinian.blog.common.Result;
import com.jinian.blog.entity.*;
import com.jinian.blog.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台 - 统计数据
 */
@Tag(name = "管理后台-统计", description = "统计数据接口")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final MessageMapper messageMapper;
    private final WishMapper wishMapper;
    private final LinkMapper linkMapper;

    @Operation(summary = "获取概览统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> stats = new HashMap<>();

        // 用户数量
        stats.put("userCount", userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getDeleted, 0)));

        // 文章数量
        stats.put("articleCount", articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getDeleted, 0)));

        // 已发布文章
        stats.put("publishedCount", articleMapper.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getDeleted, 0)
                        .eq(Article::getStatus, "PUBLISHED")));

        // 草稿数量
        stats.put("draftCount", articleMapper.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getDeleted, 0)
                        .eq(Article::getStatus, "DRAFT")));

        // 评论数量
        stats.put("commentCount", commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getDeleted, 0)));

        // 待审核评论
        stats.put("pendingCommentCount", commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getDeleted, 0)
                        .eq(Comment::getStatus, "PENDING")));

        // 留言数量
        stats.put("messageCount", messageMapper.selectCount(
                new LambdaQueryWrapper<Message>().eq(Message::getDeleted, 0)));

        // 许愿数量
        stats.put("wishCount", wishMapper.selectCount(
                new LambdaQueryWrapper<Wish>().eq(Wish::getDeleted, 0)));

        // 友链数量
        stats.put("linkCount", linkMapper.selectCount(
                new LambdaQueryWrapper<Link>().eq(Link::getDeleted, 0)));

        return Result.success(stats);
    }
}