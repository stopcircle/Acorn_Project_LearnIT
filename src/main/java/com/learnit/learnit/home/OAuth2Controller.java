package com.learnit.learnit.home;

import com.learnit.learnit.user.dto.OAuthUserDTO;
import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.service.OAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuth2Controller {

    private final OAuthService oAuthService;

    /**
     * OAuth2 로그인 성공 후 콜백 처리
     * Spring Security OAuth2가 자동으로 처리하므로 실제로는
     * SecurityConfig에서 설정한 successHandler에서 처리됩니다.
     * 
     * 이 메서드는 필요시 추가 로직을 위해 남겨둡니다.
     */
    @GetMapping("/callback/kakao")
    public String kakaoCallback(@AuthenticationPrincipal OAuth2User oAuth2User, HttpSession session) {
        return processOAuth2Login(oAuth2User, "KAKAO", session);
    }

    @GetMapping("/callback/google")
    public String googleCallback(@AuthenticationPrincipal OAuth2User oAuth2User, HttpSession session) {
        return processOAuth2Login(oAuth2User, "GOOGLE", session);
    }

    /**
     * OAuth2 사용자 정보 처리 및 세션 생성
     */
    private String processOAuth2Login(OAuth2User oAuth2User, String provider, HttpSession session) {
        try {
            OAuthUserDTO oauthUserDTO = extractOAuthUserInfo(oAuth2User, provider);
            UserDTO user = oAuthService.processOAuthUser(oauthUserDTO);

            // 세션에 사용자 정보 저장
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getName());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("nickname", user.getNickname());
            session.setAttribute("profileImageUrl", user.getProfileImageUrl());

            return "redirect:/home";
        } catch (Exception e) {
            return "redirect:/login?error=oauth_error";
        }
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

