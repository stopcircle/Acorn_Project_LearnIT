package com.learnit.learnit.mypage.controller;

import com.learnit.learnit.mypage.dto.DashboardDTO;
import com.learnit.learnit.mypage.service.DashboardService;
import com.learnit.learnit.mypage.service.UserIdentityService;
import com.learnit.learnit.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final DashboardService dashboardService;
    private final UserIdentityService userIdentityService;

    @GetMapping("/mypage")
    public String myPage() {
        // 마이페이지 기본 라우팅: /mypage → 대시보드 호출
        return "redirect:/mypage/dashboard";
    }

    @GetMapping("/mypage/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
        Long userId = userIdentityService.requireCurrentUserId(session);
        
        DashboardDTO dashboard = dashboardService.getDashboardData(userId);
        model.addAttribute("dashboard", dashboard);
        
        // 사용자 정보 조회 및 추가 (식별 서비스 사용)
        UserDTO user = userIdentityService.getUserById(userId);
        model.addAttribute("user", user);
        
        return "mypage/dashboard/dashboard";
    }
}

