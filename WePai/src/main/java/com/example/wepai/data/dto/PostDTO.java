package com.example.wepai.data.dto;

import lombok.Data;

@Data
public class PostDTO {
    private String type;
    private String title;
    private String content;
    private String images; // JSON string of urls
}