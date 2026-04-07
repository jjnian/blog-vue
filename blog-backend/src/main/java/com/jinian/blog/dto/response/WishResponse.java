package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 许愿响应
 */
@Data
@Builder
public class WishResponse {

    private Long id;
    private String content;
    private String guestName;
    private String guestAvatar;
    private String color;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private LocalDateTime createdAt;
}