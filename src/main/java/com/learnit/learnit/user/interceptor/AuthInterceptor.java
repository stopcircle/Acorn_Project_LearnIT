package com.learnit.learnit.user.interceptor;

import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // 공개 경로는 통과
        if (isPublicPath(requestURI)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        
        // 세션이 없거나 userId가 없으면 로그인하지 않은 상태
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("/login");
            return false;
        }

        // 세션에서 userId 가져오기
        Long userId = (Long) session.getAttribute("userId");
        
        // DB에서 실제 status 조회
        UserDTO user = userMapper.selectUserById(userId);
        if (user == null) {
            session.invalidate();
            response.sendRedirect("/login?error=user_not_found");
            return false;
        }

        String status = user.getStatus();
        
        // BANNED 또는 DELETE 상태
        if ("BANNED".equals(status) || "DELETE".equals(status)) {
            session.invalidate();
            response.sendRedirect("/login?error=banned_or_deleted");
            return false;
        }

        // SIGNUP_PENDING 또는 PROFILE_REQUIRED 상태 (가입 미완료)
        if ("SIGNUP_PENDING".equals(status) || "PROFILE_REQUIRED".equals(status)) {
            // 가입 완료 페이지로 리다이렉트
            if (!requestURI.startsWith("/signup/complete") && !requestURI.startsWith("/signup/additional-info")) {
                response.sendRedirect("/signup/complete");
                return false;
            }
            // 가입 완료 페이지 자체는 접근 허용
            return true;
        }

        // ACTIVE 상태만 정상 접근 허용
        if (!"ACTIVE".equals(status)) {
            session.invalidate();
            response.sendRedirect("/login?error=invalid_status");
            return false;
        }

        // 마이페이지 접근 시 ACTIVE 상태 확인 (이중 체크)
        if (requestURI.startsWith("/mypage")) {
            if (!"ACTIVE".equals(status)) {
                response.sendRedirect("/signup/complete");
                return false;
            }
        }

        // 세션에 최신 status 정보 업데이트
        session.setAttribute("status", status);

        return true;
    }

    private boolean isPublicPath(String requestURI) {
        return requestURI.equals("/") 
            || requestURI.equals("/login") 
            || requestURI.equals("/signup") 
            || requestURI.equals("/find")
            || requestURI.startsWith("/home")
            || requestURI.startsWith("/CourseList")
            || requestURI.startsWith("/search")
            || requestURI.startsWith("/notice")
            || requestURI.startsWith("/oauth2")
            || requestURI.startsWith("/login/oauth2")
            || requestURI.startsWith("/api")
            || requestURI.startsWith("/images")
            || requestURI.startsWith("/css")
            || requestURI.startsWith("/js")
            || requestURI.startsWith("/files")
            || requestURI.startsWith("/error");
    }
}

