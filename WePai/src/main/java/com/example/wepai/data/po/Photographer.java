package com.example.wepai.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("photographer")
public class Photographer {
    @TableId(type = IdType.INPUT)
    private String casId;
    private String style;
    private String equipment;
    private String type;
    private Integer orderCount;
}
