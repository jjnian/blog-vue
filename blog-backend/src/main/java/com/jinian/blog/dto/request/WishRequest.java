package com.jinian.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 许愿请求
 */
@Data
public class WishRequest {

    @NotBlank(message = "许愿内容不能为空")
    private String content;

    private String guestName;

    private String color;

    private BigDecimal positionX;

    private BigDecimal positionY;
}