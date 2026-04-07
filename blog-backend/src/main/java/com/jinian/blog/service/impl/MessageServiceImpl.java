package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.dto.request.MessageRequest;
import com.jinian.blog.dto.response.MessageResponse;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.entity.Message;
import com.jinian.blog.mapper.MessageMapper;
import com.jinian.blog.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 留言墙服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public PageResponse<MessageResponse> getMessages(int page, int size) {
        Page<Message> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getDeleted, 0)
               .eq(Message::getStatus, "ACTIVE")
               .orderByDesc(Message::getCreatedAt);

        Page<Message> messagePage = messageMapper.selectPage(pageParam, wrapper);

        List<MessageResponse> records = messagePage.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<MessageResponse>builder()
                .records(records)
                .total(messagePage.getTotal())
                .pages(messagePage.getPages())
                .current(messagePage.getCurrent())
                .size(messagePage.getSize())
                .build();
    }

    @Override
    public MessageResponse createMessage(MessageRequest request, Long userId, String ipAddress) {
        Message message = new Message();
        message.setContent(request.getContent());
        message.setUserId(userId);
        message.setGuestName(request.getGuestName());
        message.setAnimationType(request.getAnimationType() != null ? request.getAnimationType() : "scroll");
        message.setColor(request.getColor());
        message.setFontSize(request.getFontSize());
        message.setIpAddress(ipAddress);
        message.setStatus("ACTIVE");
        message.setCreatedAt(LocalDateTime.now());

        messageMapper.insert(message);
        return toResponse(message);
    }

    @Override
    public void deleteMessage(Long id) {
        messageMapper.deleteById(id);
    }

    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .guestName(message.getGuestName())
                .guestAvatar(message.getGuestAvatar())
                .animationType(message.getAnimationType())
                .color(message.getColor())
                .fontSize(message.getFontSize())
                .createdAt(message.getCreatedAt())
                .build();
    }
}