package com.learnit.learnit.mypage.dto;

import lombok.Data;

/**
 * 개인정보 수정 요청 DTO
 */
@Data
public class ProfileUpdateDTO {
    private String name; // 이름
    private String password; // 비밀번호 (변경 시에만)
    private String confirmPassword; // 비밀번호 확인
    private String email; // 이메일
    private String phone; // 전화번호
    private String githubUrl; // 깃허브 주소
    private String region; // 주 활동 지역
}

