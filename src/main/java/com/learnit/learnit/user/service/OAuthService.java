package com.learnit.learnit.user.service;

import com.learnit.learnit.user.dto.OAuthUserDTO;
import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserMapper userMapper;

    /**
     * OAuth 사용자로 로그인 또는 회원가입
     */
    @Transactional
    public UserDTO processOAuthUser(OAuthUserDTO oauthUser) {
        // 기존 사용자 확인
        UserDTO existingUser = userMapper.selectUserByProvider(
            oauthUser.getProvider(), 
            oauthUser.getProviderId()
        );

        UserDTO user;
        if (existingUser != null) {
            // 기존 사용자 업데이트 (프로필 이미지 등 최신 정보 반영)
            user = existingUser;
            user.setNickname(oauthUser.getNickname());
            user.setProfileImageUrl(oauthUser.getProfileImageUrl());
            user.setEmail(oauthUser.getEmail());
            userMapper.insertOrUpdateOAuthUser(user);
        } else {
            // 새 사용자 생성
            user = new UserDTO();
            user.setEmail(oauthUser.getEmail());
            user.setName(oauthUser.getNickname() != null ? oauthUser.getNickname() : oauthUser.getEmail()); // 닉네임 또는 이메일을 name으로 사용
            user.setNickname(oauthUser.getNickname());
            user.setProfileImageUrl(oauthUser.getProfileImageUrl());
            user.setProvider(oauthUser.getProvider());
            user.setProviderId(oauthUser.getProviderId());
            user.setStatus("PROFILE_REQUIRED");
            
            userMapper.insertOrUpdateOAuthUser(user);
        }
        
        // 생성/업데이트된 사용자 다시 조회하여 반환
        UserDTO result = userMapper.selectUserByProvider(oauthUser.getProvider(), oauthUser.getProviderId());
        if (result == null) {
            throw new RuntimeException("OAuth 사용자 처리 실패");
        }
        return result;
    }

    /**
     * OAuth 사용자 조회
     */
    public Optional<UserDTO> findByProvider(String provider, String providerId) {
        UserDTO user = userMapper.selectUserByProvider(provider, providerId);
        return Optional.ofNullable(user);
    }
}

