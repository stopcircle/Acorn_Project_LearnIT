package com.learnit.learnit.user.dto;

import lombok.Data;

@Data
public class OAuthUserDTO {
    private String provider; // KAKAO, GOOGLE
    private String providerId; // OAuth provider user ID
    private String email;
    private String nickname;
    private String profileImageUrl;
}

