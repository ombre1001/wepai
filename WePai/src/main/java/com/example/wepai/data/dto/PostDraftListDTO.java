package com.example.wepai.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostDraftListDTO {
    private Long postId;
    private String title;          // 帖子标题，作为列表展示字段
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}