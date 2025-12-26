package com.learnit.learnit.mypage.mapper;

import com.learnit.learnit.mypage.dto.ProfileDTO;
import com.learnit.learnit.user.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileMapper {
    
    /**
     * 프로필 정보 조회 (상세 정보 포함)
     */
    ProfileDTO selectProfileById(@Param("userId") Long userId);
    
    /**
     * 사용자 기본 정보 조회 (개인정보 수정 페이지용)
     */
    UserDTO selectUserForEdit(@Param("userId") Long userId);
    
    /**
     * 개인정보 수정
     */
    int updateProfile(@Param("userId") Long userId, 
                      @Param("name") String name,
                      @Param("email") String email,
                      @Param("phone") String phone,
                      @Param("githubUrl") String githubUrl,
                      @Param("region") String region);
    
    /**
     * 비밀번호 변경
     */
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);
}

