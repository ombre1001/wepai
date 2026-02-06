package com.example.wepai.data.dto;

import lombok.Data;

public class UserUpdateDTO {
    private String nickname;
    private String avatarUrl; // 新增头像修改支持
    private Integer sex;
    private String phone;
    private String detail;



    public String getNickname( ) {return nickname;}
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Integer getSex(){ return sex;}
    public void setSex(Integer sex){this.sex = sex;}

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    private PhotographerDTO photographer;

    public PhotographerDTO getPhotographer() {
        return photographer;
    }

    @Data
    public static class PhotographerDTO {
        private String style;
        private String equipment;
        private String type;
    }
}