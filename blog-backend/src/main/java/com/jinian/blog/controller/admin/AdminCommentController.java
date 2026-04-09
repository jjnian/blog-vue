package com.jinian.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.entity.Comment;
import com.jinian.blog.mapper.CommentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 评论管理
 */
@Tag(name = "管理后台-评论", description = "评论管理接口")
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentMapper commentMapper;

    @Operation(summary = "获取评论列表")
    @GetMapping
    public Result<PageResponse<Comment>> getComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) String keyword) {

        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getDeleted, 0);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Comment::getStatus, status);
        }
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Comment::getContent, keyword);
        }

        wrapper.orderByDesc(Comment::getCreatedAt);

        Page<Comment> commentPage = commentMapper.selectPage(pageParam, wrapper);

        return Result.success(PageResponse.<Comment>builder()
                .records(commentPage.getRecords())
                .total(commentPage.getTotal())
                .pages(commentPage.getPages())
                .current(commentPage.getCurrent())
                .size(commentPage.getSize())
                .build());
    }

    @Operation(summary = "审核评论")
    @PutMapping("/{id}/status")
    public Result<Void> updateCommentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        String status = body.get("status");
        comment.setStatus(status);
        commentMapper.updateById(comment);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        commentMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除评论")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            commentMapper.deleteById(id);
        }
        return Result.success();
    }
}
