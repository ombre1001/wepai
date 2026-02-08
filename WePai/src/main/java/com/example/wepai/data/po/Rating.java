package com.example.wepai.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ratings")
public class Rating {
    @TableId(type = IdType.AUTO)
    private Long ratingId;

    private Long orderId;       // 关联订单
    private String reviewerId;  // 评价人
    private String targetId;    // 被评价人
    private Double score;       // 评分 (1.0 - 5.0)
    private String content;     // 评价内容

    private LocalDateTime createdAt;
}