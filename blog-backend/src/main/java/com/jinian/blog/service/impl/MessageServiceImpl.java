package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.dto.request.MessageRequest;
import com.jinian.blog.dto.response.MessageResponse;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.entity.Message;
import com.jinian.blog.entity.User;
import com.jinian.blog.mapper.MessageMapper;
import com.jinian.blog.mapper.UserMapper;
import com.jinian.blog.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 留言墙服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

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
        User user = userId != null ? userMapper.selectById(userId) : null;

        message.setContent(request.getContent());
        message.setUserId(userId);
        message.setGuestName(resolveGuestName(request, user));
        message.setGuestAvatar(resolveGuestAvatar(request, user));
        message.setAnimationType(request.getAnimationType() != null ? request.getAnimationType() : "scroll");
        message.setColor(request.getColor() != null ? request.getColor() : "#ffffff");
        message.setFontSize(request.getFontSize() != null ? request.getFontSize() : "1rem");
        message.setIpAddress(ipAddress);
        message.setStatus("ACTIVE");

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

    private String resolveGuestName(MessageRequest request, User user) {
        if (request.getGuestName() != null && !request.getGuestName().isBlank()) {
            return request.getGuestName();
        }
        if (user != null && user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user != null && user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return "访客";
    }
    private String resolveGuestAvatar(MessageRequest request, User user) {
        if (request.getGuestAvatar() != null && !request.getGuestAvatar().isBlank()) {
            return request.getGuestAvatar();
        }
        if (user != null && user.getAvatar() != null && !user.getAvatar().isBlank()) {
            return user.getAvatar();
        }
        String seed = resolveGuestName(request, user);
        return "https://picsum.photos/seed/" + Math.abs(Objects.hash(seed)) + "/100/100";
    }
}
