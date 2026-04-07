package com.jinian.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 留言请求
 */
@Data
public class MessageRequest {

    @NotBlank(message = "留言内容不能为空")
    private String content;

    private String guestName;

    private String animationType;

    private String color;

    private String fontSize;
}