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
@TableName("user")
public class User {
    @TableId(type = IdType.INPUT)
     private String casId;
     private String name;
     private String nickname;
     private String avatarUrl;
     private Integer role;//0:Admin,1:customer,2:photographer
     private Integer sex;
     private String phone;
     private String detail;
}
