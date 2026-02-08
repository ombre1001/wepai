package com.example.wepai.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("posts")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long postId;
    private String userId;      // 发布者
    private Integer type;       // 1: 约拍需求(客户发), 2: 作品展示(摄影师发)
    private String title;
    private String content;
    private String images;      // 图片JSON
    private Long orderId;       // 如果是需求帖，关联生成的空白订单ID
    private Integer status;     // 1:展示中, 0:已接单/关闭
    private LocalDateTime createdAt;
}