package com.learnit.learnit.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long userId;
    private String name;
    private String password;
    private String email;
    private String nickname;
    private String phone;
    private String region;
    private String githubUrl; // 깃허브 URL
    private String profileImageUrl;
    private String status; // SIGNUP_PENDING, PROFILE_REQUIRED, ACTIVE, BANNED, DELETE
    private String provider; // LOCAL, KAKAO, GOOGLE
    private String providerId; // OAuth provider user ID
    private String emailVerified; // 'Y' or 'N'
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

