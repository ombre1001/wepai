package com.example.demo.service;

import com.example.demo.data.dto.UserUpdateDTO;
import com.example.demo.data.po.User;
import com.example.demo.data.vo.Result;
import com.example.demo.mapper.UserMapper;
import com.example.demo.utils.BcryptUtils;
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



    public ResponseEntity<Result> getProfile(String userId){
        User user = userMapper.getUserById(userId);
        if(user == null){
            throw new RuntimeException("用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());

        return Result.success(userInfo, "获取用户信息成功");
    }

    @Transactional
    public ResponseEntity<Result> updateProfile(String userId, UserUpdateDTO updateDTO){
        User existingUser = userMapper.getUserById(userId);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        if(updateDTO.getUsername() != null && existingUser.getUsername().equals(updateDTO.getUsername())){
            if(isExisted(updateDTO.getUsername())){
                throw new RuntimeException("用户名已经被使用");
            }
        }

        User userToUpdate = new User();
        userToUpdate.setId(Integer.valueOf(userId));
        userToUpdate.setUsername(updateDTO.getUsername() != null ? updateDTO.getUsername() : existingUser.getUsername());
        userToUpdate.setEmail(updateDTO.getEmail() != null ? updateDTO.getEmail() : existingUser.getEmail());
        userToUpdate.setPhone(updateDTO.getPhone() != null ? updateDTO.getPhone() : existingUser.getPhone());


        int rowsAffected = userMapper.updateUser(userToUpdate);
        if (rowsAffected == 0) {
            throw new RuntimeException("更新用户信息失败");
        }

        User updatedUser = userMapper.getUserById(userId);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", updatedUser.getId());
        userInfo.put("username", updatedUser.getUsername());
        userInfo.put("email", updatedUser.getEmail());
        userInfo.put("phone", updatedUser.getPhone());

        return Result.success(userInfo, "用户信息更新成功");

    }

    @Transactional
    public ResponseEntity<Result> updatePassword(String userId, String oldPassword, String newPassword) {
        User user = userMapper.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!BcryptUtils.verifyPasswd(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        User userToUpdate = new User();
        userToUpdate.setId(Integer.valueOf(userId));
        userToUpdate.setPassword(BcryptUtils.encrypt(newPassword));

        int rowsAffected = userMapper.updateUserPassword(userToUpdate);
        if (rowsAffected == 0) {
            throw new RuntimeException("密码修改失败");
        }

        return Result.success(null, "密码修改成功");
    }

    @Transactional
    public ResponseEntity<Result> deleteAccount(String userId) {
        try {
            User user = userMapper.getUserById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 物理删除：直接从数据库移除用户记录
            int rowsAffected = userMapper.deleteUserById(userId);
            if (rowsAffected == 0) {
                throw new RuntimeException("账户注销失败");
            }

            return Result.success(null, "账户注销成功");

        } catch (Exception e) {
            throw new RuntimeException("账户注销失败");
        }
    }
    boolean isExisted(String userName) {
        Integer count = userMapper.getUserId(userName);
        return count != null && count > 0;
    }
}

