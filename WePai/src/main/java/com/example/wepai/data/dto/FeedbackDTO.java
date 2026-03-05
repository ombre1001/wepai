package com.example.wepai.data.dto;

import lombok.Data;

@Data
public class FeedbackDTO {
    private String content;
    private String contact; // 联系方式
    private String images;  // 图片 JSON 数组
    private String status;
}
