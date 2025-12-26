package com.learnit.learnit.mypage.service;

import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 마이페이지 개인 식별 관련 서비스
 * 사용자 식별 및 인증 정보를 일관되게 처리
 */
@Service
@RequiredArgsConstructor
public class UserIdentityService {

    private final UserMapper userMapper;

    /**
     * 세션에서 현재 로그인한 사용자 ID 조회
     * 
     * @param session HTTP 세션
     * @return 사용자 ID, 로그인하지 않은 경우 null
     */
    public Long getCurrentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute("userId");
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     * 
     * @param session HTTP 세션
     * @return 사용자 정보, 로그인하지 않은 경우 null
     */
    public UserDTO getCurrentUser(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return null;
        }
        return userMapper.selectUserById(userId);
    }

    /**
     * 현재 로그인한 사용자 ID 조회 및 검증
     * 
     * @param session HTTP 세션
     * @return 사용자 ID
     * @throws IllegalStateException 로그인하지 않은 경우
     */
    public Long requireCurrentUserId(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userId;
    }

    /**
     * 현재 로그인한 사용자 정보 조회 및 검증
     * 
     * @param session HTTP 세션
     * @return 사용자 정보
     * @throws IllegalStateException 로그인하지 않았거나 사용자를 찾을 수 없는 경우
     */
    public UserDTO requireCurrentUser(HttpSession session) {
        Long userId = requireCurrentUserId(session);
        UserDTO user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        return user;
    }

    /**
     * 사용자 ID로 사용자 정보 조회
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     */
    public UserDTO getUserById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
        UserDTO user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        return user;
    }

    /**
     * 세션의 사용자 ID와 요청한 사용자 ID가 일치하는지 확인
     * 
     * @param session HTTP 세션
     * @param requestedUserId 요청한 사용자 ID
     * @return 일치 여부
     */
    public boolean isCurrentUser(HttpSession session, Long requestedUserId) {
        Long currentUserId = getCurrentUserId(session);
        return currentUserId != null && currentUserId.equals(requestedUserId);
    }

    /**
     * 세션의 사용자 ID와 요청한 사용자 ID가 일치하는지 확인 및 검증
     * 
     * @param session HTTP 세션
     * @param requestedUserId 요청한 사용자 ID
     * @throws IllegalStateException 일치하지 않는 경우
     */
    public void requireCurrentUser(HttpSession session, Long requestedUserId) {
        if (!isCurrentUser(session, requestedUserId)) {
            throw new IllegalStateException("본인의 정보만 조회할 수 있습니다.");
        }
    }
}

