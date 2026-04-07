package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jinian.blog.common.exception.ResourceNotFoundException;
import com.jinian.blog.dto.request.CommentRequest;
import com.jinian.blog.dto.response.CommentResponse;
import com.jinian.blog.entity.Comment;
import com.jinian.blog.mapper.CommentMapper;
import com.jinian.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    @Override
    public List<CommentResponse> getCommentsByArticleId(Long articleId) {
        List<Comment> comments = commentMapper.selectByArticleId(articleId);
        return buildCommentTree(comments, null);
    }

    @Override
    public CommentResponse createComment(CommentRequest request, Long userId, String ipAddress) {
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setArticleId(request.getArticleId());
        comment.setParentId(request.getParentId());
        comment.setUserId(userId);
        comment.setGuestName(request.getGuestName());
        comment.setGuestEmail(request.getGuestEmail());
        comment.setStatus(userId != null ? "APPROVED" : "PENDING");
        comment.setLikes(0);
        comment.setIpAddress(ipAddress);
        comment.setCreatedAt(LocalDateTime.now());

        commentMapper.insert(comment);
        return toResponse(comment);
    }

    @Override
    public void deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ResourceNotFoundException("评论", id);
        }
        commentMapper.deleteById(id);
    }

    @Override
    public void auditComment(Long id, String status) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ResourceNotFoundException("评论", id);
        }
        comment.setStatus(status);
        commentMapper.updateById(comment);
    }

    private List<CommentResponse> buildCommentTree(List<Comment> comments, Long parentId) {
        Map<Long, List<Comment>> commentMap = comments.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));

        List<Comment> children = commentMap.getOrDefault(parentId == null ? 0L : parentId, new ArrayList<>());
        return children.stream()
                .map(comment -> {
                    CommentResponse response = toResponse(comment);
                    response.setChildren(buildCommentTree(comments, comment.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse.UserInfo.UserInfoBuilder userBuilder = CommentResponse.UserInfo.builder();

        if (comment.getUserId() != null) {
            userBuilder.id(comment.getUserId());
        } else {
            userBuilder.name(comment.getGuestName())
                      .email(comment.getGuestEmail());
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .articleId(comment.getArticleId())
                .parentId(comment.getParentId())
                .user(userBuilder.build())
                .status(comment.getStatus())
                .likes(comment.getLikes())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}