package com.example.wepai.data.dto;

import lombok.Data;

@Data
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private Integer type; // 0:系统通知, 1:活动公告
}
