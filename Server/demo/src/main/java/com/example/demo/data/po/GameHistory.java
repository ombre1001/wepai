package com.example.demo.data.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("game_history")
public class GameHistory {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer gameId;
    private Integer score;
    private String additionalInfo; //具体信息
}
