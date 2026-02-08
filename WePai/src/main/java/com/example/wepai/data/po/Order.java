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
@TableName("orders") // 对应数据库表名 orders
public class Order {
    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String customerId;      // 对应 User.casId
    private String photographerId;  // 对应 Photographer.casId

    private Integer type;           // 1-毕业照, 2-写真, 3-活动等

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shootTime; // 拍摄时间

    private String location;        // 拍摄地点
    private BigDecimal price;       // 价格
    private String remark;          // 备注

    /**
     * 订单状态:
     * 0: 待接单 (Pending)
     * 1: 待支付 (Accepted/Unpaid)
     * 2: 支付中 (Paid/To be shot)
     * 3: 已完成 (Completed)
     * -1: 已取消 (Cancelled)
     * -2: 已拒绝 (Rejected)
     */
    private Integer status;//0: 待接单(大厅中), 1: 已接单/待支付, 2: 待交付, 3: 已完成

    private Integer paymentStatus;  // 0:未付, 1:已付
    private String deliverUrl;      // 成片交付链接

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
