package com.example.demo.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("game")
public class Game {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;

}
