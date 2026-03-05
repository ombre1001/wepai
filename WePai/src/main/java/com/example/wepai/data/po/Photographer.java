package com.example.wepai.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("photographer")
public class Photographer {
    @TableId(type = IdType.INPUT)
    private String casId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> style;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> equipment;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> type;
    private Integer orderCount;
}
