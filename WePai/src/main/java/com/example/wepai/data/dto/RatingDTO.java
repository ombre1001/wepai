package com.example.wepai.data.dto;

import lombok.Data;

@Data
public class RatingDTO {
    private Long orderId;
    private Double score;
    private String content;
}
