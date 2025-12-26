package com.learnit.learnit.mypage.controller;

import com.learnit.learnit.mypage.dto.ProfileDTO;
import com.learnit.learnit.mypage.dto.ProfileUpdateDTO;
import com.learnit.learnit.mypage.service.ProfileService;
import com.learnit.learnit.mypage.service.UserIdentityService;
import com.learnit.learnit.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final UserIdentityService userIdentityService;

    /**
     * 프로필 조회 페이지 (/mypage/profile)
     */
    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
        Long userId = userIdentityService.requireCurrentUserId(session);

        ProfileDTO profile = profileService.getProfile(userId);
        model.addAttribute("profile", profile);
        
        // 사용자 기본 정보도 함께 전달 (레이아웃용)
        UserDTO user = profileService.getUserForEdit(userId);
        model.addAttribute("user", user);

        return "mypage/profile/profile";
    }

    /**
     * 개인정보 수정 페이지 (/mypage/settings)
     */
    @GetMapping("/settings")
    public String settingsPage(HttpSession session, Model model) {
        // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
        Long userId = userIdentityService.requireCurrentUserId(session);

        UserDTO user = profileService.getUserForEdit(userId);
        model.addAttribute("user", user);
        
        // 수정 폼용 DTO 초기화
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO();
        updateDTO.setName(user.getName()); // 이름은 name으로 설정
        updateDTO.setEmail(user.getEmail());
        updateDTO.setPhone(user.getPhone());
        updateDTO.setGithubUrl(user.getGithubUrl());
        updateDTO.setRegion(user.getRegion());
        model.addAttribute("updateDTO", updateDTO);

        return "mypage/profile/profile_edit";
    }

    /**
     * 개인정보 수정 처리
     */
    @PostMapping("/settings/update")
    public String updateProfile(ProfileUpdateDTO updateDTO, HttpSession session, RedirectAttributes redirectAttributes) {
        // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
        Long userId = userIdentityService.requireCurrentUserId(session);

        try {
            // 비밀번호 변경이 있는 경우
            if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
                profileService.updatePassword(
                    userId,
                    null, // 현재 비밀번호 확인은 추후 추가
                    updateDTO.getPassword(),
                    updateDTO.getConfirmPassword()
                );
                redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
            } else {
                // 개인정보 수정 (비밀번호 제외)
                profileService.updateProfile(userId, updateDTO);
                redirectAttributes.addFlashAttribute("message", "개인정보가 수정되었습니다.");
            }

            return "redirect:/mypage/settings";
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/settings";
        }
    }
}

