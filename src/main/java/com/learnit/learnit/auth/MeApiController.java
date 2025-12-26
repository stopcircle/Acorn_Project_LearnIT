package com.learnit.learnit.auth;

import com.learnit.learnit.user.User;
import com.learnit.learnit.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeApiController {

    private final UserRepository userRepository;

    @Data
    @AllArgsConstructor
    static class MeResponse {
        private Long userId;
        private String name;
        private String nickname;
        private String role;
        private boolean loggedIn;
    }

    @GetMapping("/api/me")
    public MeResponse me(HttpSession session) {

        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        if (userId == null) {
            return new MeResponse(null, null, null, null, false);
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return new MeResponse(null, null, null, null, false);
        }

        return new MeResponse(
                user.getUserId(),
                user.getName(),
                user.getNickname(),
                user.getRole(),
                true
        );
    }
}
