package com.learnit.learnit.mypage.service;

import com.learnit.learnit.mypage.dto.ProfileDTO;
import com.learnit.learnit.mypage.dto.ProfileUpdateDTO;
import com.learnit.learnit.mypage.mapper.ProfileMapper;
import com.learnit.learnit.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 프로필 정보 조회
     */
    public ProfileDTO getProfile(Long userId) {
        ProfileDTO profile = profileMapper.selectProfileById(userId);
        
        // 배지, GitHub 분석, 스킬 차트, 스터디 정보는 추후 확장
        // 현재는 기본 정보만 반환
        
        return profile;
    }

    /**
     * 개인정보 수정용 사용자 정보 조회
     */
    public UserDTO getUserForEdit(Long userId) {
        return profileMapper.selectUserForEdit(userId);
    }

    /**
     * 개인정보 수정
     */
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateDTO updateDTO) {
        // 이메일 중복 확인은 추후 추가 (다른 사용자가 사용 중인지 확인)
        
        // 개인정보 수정 (비밀번호 제외)
        profileMapper.updateProfile(
            userId,
            updateDTO.getName(), // 이름 (name 필드 업데이트)
            updateDTO.getEmail(),
            updateDTO.getPhone(),
            updateDTO.getGithubUrl() != null ? updateDTO.getGithubUrl() : "",
            updateDTO.getRegion() != null ? updateDTO.getRegion() : ""
        );
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        // 새 비밀번호 확인
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        // 현재 비밀번호 확인은 컨트롤러에서 처리 (기존 비밀번호 조회 필요)
        // 비밀번호 암호화 후 업데이트
        String encodedPassword = passwordEncoder.encode(newPassword);
        profileMapper.updatePassword(userId, encodedPassword);
    }
}

