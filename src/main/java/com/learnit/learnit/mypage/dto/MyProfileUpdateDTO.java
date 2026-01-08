package com.learnit.learnit.mypage.dto;

import lombok.Data;

@Data
public class MyProfileUpdateDTO {
    private String name;
    private String password;
    private String email;
    private String phone;
    private String githubUrl;
    private String region;
}

