package com.example.wepai.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String customerId;
    private String photographerId;

    private String type;             // 修改为 String

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shootTime;

    private String duration;         // 新增：时长
    private String location;
    private Integer subjectCount;    // 新增：人数
    private BigDecimal price;
    private Boolean needEquipment;   // 新增：是否需要器材 (数据库建议用 TINYINT/BOOLEAN)
    private String contactInfo;      // 新增：联系方式
    private String remark;

    /**
     * 0: 待接单, 1: 待支付, 2: 进行中, 3: 已完成, -1: 取消, -2: 拒绝，-3:草稿
     */
    private Integer status;
    private Integer paymentStatus;
    private String deliverUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
