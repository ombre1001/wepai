package com.example.wepai.service;

import com.example.wepai.data.dto.UserUpdateDTO;
import com.example.wepai.data.po.User;
import com.example.wepai.data.vo.Result;
import com.example.wepai.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    public ResponseEntity<Result> getProfile(String casId) {
        User user = userMapper.getUserById(casId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("casId", casId); // 对应 POJO 中的 casID
        userInfo.put("name", user.getName());

        return Result.success(userInfo, "获取用户信息成功");
    }

    @Transactional
    public ResponseEntity<Result> updateProfile(String casId, UserUpdateDTO updateDTO) {
        User existingUser = userMapper.getUserById(casId);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户名是否冲突
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(existingUser.getName())) {
            if (isExisted(updateDTO.getUsername())) {
                throw new RuntimeException("用户名已经被使用");
            }
        }

        // 构造更新对象，仅操作 User 实体支持的字段
        User userToUpdate = new User();
        userToUpdate.setCasId(casId);
        userToUpdate.setName(updateDTO.getUsername() != null ? updateDTO.getUsername() : existingUser.getName());

        int rowsAffected = userMapper.updateUser(userToUpdate);
        if (rowsAffected == 0) {
            throw new RuntimeException("更新用户信息失败");
        }

        return Result.success(userToUpdate, "用户信息更新成功");
    }

    boolean isExisted(String userName) {
        // 根据数据库查询用户名是否存在
        Integer count = Integer.valueOf(userMapper.getUserId(userName));
        return count != null && count > 0;
    }
}

