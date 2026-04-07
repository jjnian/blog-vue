package com.jinian.blog.service;

import com.jinian.blog.dto.request.MessageRequest;
import com.jinian.blog.dto.response.MessageResponse;
import com.jinian.blog.dto.response.PageResponse;

/**
 * 留言墙服务接口
 */
public interface MessageService {

    PageResponse<MessageResponse> getMessages(int page, int size);

    MessageResponse createMessage(MessageRequest request, Long userId, String ipAddress);

    void deleteMessage(Long id);
}