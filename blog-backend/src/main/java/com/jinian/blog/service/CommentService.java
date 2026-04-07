package com.jinian.blog.service;

import com.jinian.blog.dto.request.CommentRequest;
import com.jinian.blog.dto.response.CommentResponse;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    List<CommentResponse> getCommentsByArticleId(Long articleId);

    CommentResponse createComment(CommentRequest request, Long userId, String ipAddress);

    void deleteComment(Long id);

    void auditComment(Long id, String status);
}