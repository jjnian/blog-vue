package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.request.MessageRequest;
import com.jinian.blog.dto.response.MessageResponse;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.service.MessageService;
import com.jinian.blog.util.SecurityUtils;
import com.jinian.blog.util.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 留言墙控制器
 */
@Tag(name = "留言墙", description = "留言墙/弹幕相关接口")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "获取留言列表")
    @GetMapping
    public Result<PageResponse<MessageResponse>> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.getMessages(page, size));
    }

    @Operation(summary = "发送留言")
    @PostMapping
    public Result<MessageResponse> createMessage(@Valid @RequestBody MessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String ipAddress = WebUtils.getClientIp();
        return Result.success(messageService.createMessage(request, userId, ipAddress));
    }

    @Operation(summary = "删除留言")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return Result.success();
    }
}