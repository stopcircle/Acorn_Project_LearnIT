package com.learnit.learnit.config;

import com.learnit.learnit.user.dto.OAuthUserDTO;
import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuthService oAuthService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // OAuth2 사용자 정보 추출
        String provider = determineProvider(oAuth2User);
        OAuthUserDTO oauthUserDTO = extractOAuthUserInfo(oAuth2User, provider);
        
        // OAuth 사용자 처리 (회원가입 또는 로그인)
        UserDTO user = oAuthService.processOAuthUser(oauthUserDTO);
        
        // 세션에 사용자 정보 저장
        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getName());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("nickname", user.getNickname());
        session.setAttribute("profileImageUrl", user.getProfileImageUrl());
        session.setAttribute("status", user.getStatus());
        
        // status에 따라 리다이렉트
        String status = user.getStatus();
        if ("SIGNUP_PENDING".equals(status) || "PROFILE_REQUIRED".equals(status)) {
            getRedirectStrategy().sendRedirect(request, response, "/signup/complete");
            return;
        }
        
        // 홈으로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, "/home");
    }

    /**
     * OAuth 제공자 식별
     */
    private String determineProvider(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // 카카오는 "id" 필드가 있음
        if (attributes.containsKey("id") && attributes.containsKey("kakao_account")) {
            return "KAKAO";
        }
        // 구글은 "sub" 필드가 있음
        if (attributes.containsKey("sub")) {
            return "GOOGLE";
        }
        
        // 기본값 (구글)
        return "GOOGLE";
    }

    /**
     * OAuth2 사용자 정보 추출 (카카오/구글)
     */
    private OAuthUserDTO extractOAuthUserInfo(OAuth2User oAuth2User, String provider) {
        OAuthUserDTO oauthUserDTO = new OAuthUserDTO();
        oauthUserDTO.setProvider(provider);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        if ("KAKAO".equals(provider)) {
            // 카카오 응답 구조에 맞게 추출
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            @SuppressWarnings("unchecked")
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            
            oauthUserDTO.setProviderId(String.valueOf(attributes.get("id")));
            oauthUserDTO.setEmail((String) kakaoAccount.get("email"));
            oauthUserDTO.setNickname((String) profile.get("nickname"));
            oauthUserDTO.setProfileImageUrl((String) profile.get("profile_image_url"));
            
        } else if ("GOOGLE".equals(provider)) {
            // 구글 응답 구조에 맞게 추출
            oauthUserDTO.setProviderId((String) attributes.get("sub"));
            oauthUserDTO.setEmail((String) attributes.get("email"));
            oauthUserDTO.setNickname((String) attributes.get("name"));
            oauthUserDTO.setProfileImageUrl((String) attributes.get("picture"));
        }

        return oauthUserDTO;
    }
}

