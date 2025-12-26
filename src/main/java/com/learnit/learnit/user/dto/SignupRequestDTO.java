package com.learnit.learnit.user.dto;

import lombok.Data;

@Data
public class SignupRequestDTO {
    private String name;
    private String password;
    private String confirmPassword;
    private String email;
    private String nickname;
    private String phone;
    private String region;
    private String consentReceive; // 수신 동의
    private Boolean consentSms; // SMS 수신 동의
}

