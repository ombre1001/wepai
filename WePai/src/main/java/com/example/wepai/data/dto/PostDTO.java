package com.example.wepai.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostDTO {
    private Long postId;
    private String type;
    private String title;
    private String content;
    private List<String> images;
}