package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.request.CommentRequest;
import com.jinian.blog.dto.response.CommentResponse;
import com.jinian.blog.service.CommentService;
import com.jinian.blog.util.SecurityUtils;
import com.jinian.blog.util.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@Tag(name = "评论", description = "评论相关接口")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取文章评论")
    @GetMapping("/article/{articleId}")
    public Result<List<CommentResponse>> getArticleComments(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentsByArticleId(articleId));
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String ipAddress = WebUtils.getClientIp();
        return Result.success(commentService.createComment(request, userId, ipAddress));
    }

    @Operation(summary = "点赞评论")
    @PostMapping("/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}