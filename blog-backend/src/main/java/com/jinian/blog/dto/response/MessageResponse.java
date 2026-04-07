package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言响应
 */
@Data
@Builder
public class MessageResponse {

    private Long id;
    private String content;
    private String guestName;
    private String guestAvatar;
    private String animationType;
    private String color;
    private String fontSize;
    private LocalDateTime createdAt;
}