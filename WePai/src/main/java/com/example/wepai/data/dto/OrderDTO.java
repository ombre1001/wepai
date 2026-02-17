package com.example.wepai.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    // --- 创建时使用 ---
    private String photographerId; // 是否预约特定摄影师（有值即为预约）

    private String type;           // 拍摄类型和风格

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shootTime; // 拍摄时间

    private String duration;       // 大约拍摄时长
    private String location;       // 地点
    private Integer subjectCount;  // 拍摄人数
    private BigDecimal price;      // 报酬
    private Boolean needEquipment; // 是否需要专业设备
    private String contactInfo;    // 联系方式
    private String remark;         // 其他问题和需求

    // --- 操作时使用 ---
    private Long orderId;
    private String action; // ACCEPT, REJECT, PAY, DELIVER, COMPLETE
    private String deliverUrl;
}