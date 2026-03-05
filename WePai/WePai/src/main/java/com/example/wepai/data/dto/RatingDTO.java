package com.example.wepai.data.dto;

import lombok.Data;

@Data
public class RatingDTO {
    private Long orderId;
    private Integer photoScore; // 摄影质量
    private Integer timeScore;  // 准时程度
    private Integer commScore;  // 沟通效率
    private Double score;
    private String content;
}
