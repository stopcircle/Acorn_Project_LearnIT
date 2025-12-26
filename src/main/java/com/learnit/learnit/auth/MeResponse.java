package com.learnit.learnit.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeResponse {
    private Long userId;
    private String name;
    private String nickname;
    private String role;
    private boolean loggedIn;
}