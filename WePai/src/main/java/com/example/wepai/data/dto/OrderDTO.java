package com.example.wepai.data.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {
    // 创建时使用
    private String photographerId;
    private Integer type;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shootTime;
    private String location;
    private BigDecimal price;
    private String remark;

    // 操作时使用
    private Long orderId;
    private String action; // ACCEPT, REJECT, PAY, DELIVER, COMPLETE
    private String deliverUrl; // 交付时必填
}