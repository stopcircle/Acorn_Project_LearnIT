package com.learnit.learnit.user.mapper;

import com.learnit.learnit.user.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // 회원가입
    int insertUser(UserDTO userDTO);
    
    // 이메일로 사용자 조회
    UserDTO selectUserByEmail(@Param("email") String email);
    
    // 사용자 ID로 조회
    UserDTO selectUserById(@Param("userId") Long userId);
    
    // 사용자명 중복 확인
    boolean existsByUsername(@Param("username") String username);
    
    // 이메일 중복 확인
    boolean existsByEmail(@Param("email") String email);
    
    // OAuth 사용자 조회 (provider + providerId)
    UserDTO selectUserByProvider(@Param("provider") String provider, @Param("providerId") String providerId);
    
    // OAuth 사용자 생성 또는 업데이트
    int insertOrUpdateOAuthUser(UserDTO userDTO);
    
    // 이메일 인증 번호 저장 (인증 테이블이 있다고 가정)
    int saveEmailVerificationCode(@Param("email") String email, @Param("code") String code);
    
    // 이메일 인증 번호 검증
    boolean verifyEmailCode(@Param("email") String email, @Param("code") String code);
    
    // 사용자 상태 업데이트
    int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);
    
    // 비밀번호 업데이트
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);
}

