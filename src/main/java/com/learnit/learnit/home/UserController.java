package com.learnit.learnit.home;

import com.learnit.learnit.user.dto.LoginRequestDTO;
import com.learnit.learnit.user.dto.SignupRequestDTO;
import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.mapper.UserMapper;
import com.learnit.learnit.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * 로그인 페이지 이동
     */
    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    /**
     * 회원가입 페이지 이동
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "user/signup";
    }

    /**
     * 아이디/비밀번호 찾기 페이지 이동
     */
    @GetMapping("/find")
    public String findPage() {
        return "user/find";
    }

    /**
     * 가입 완료 페이지 (추가 정보 입력 필요)
     */
    @GetMapping("/signup/complete")
    public String signupCompletePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        UserDTO user = userMapper.selectUserById(userId);
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("status", user.getStatus());
        
        return "user/signup-complete"; // 템플릿 필요 (추후 생성)
    }

    /**
     * 로그인 처리
     */
    @PostMapping("/login")
    public String login(LoginRequestDTO loginRequest, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Optional<UserDTO> userOpt = userService.login(loginRequest);
            
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
                return "redirect:/login";
            }

            UserDTO user = userOpt.get();
            
            // 세션에 사용자 정보 저장
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getName());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("nickname", user.getNickname());
            session.setAttribute("profileImageUrl", user.getProfileImageUrl());
            session.setAttribute("status", user.getStatus()); // status 저장
            
            // status에 따라 리다이렉트
            String status = user.getStatus();
            if ("SIGNUP_PENDING".equals(status) || "PROFILE_REQUIRED".equals(status)) {
                return "redirect:/signup/complete";
            }
            
            return "redirect:/home";
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(SignupRequestDTO signupRequest, RedirectAttributes redirectAttributes) {
        try {
            userService.signup(signupRequest);
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home";
    }

    /**
     * 사용자명 중복 확인 (AJAX)
     */
    @GetMapping("/api/check-username")
    public String checkUsername(@RequestParam String username, Model model) {
        boolean exists = userService.existsByUsername(username);
        model.addAttribute("exists", exists);
        return "jsonView"; // JSON 응답을 위한 뷰 (필요시 별도 처리)
    }

    /**
     * 이메일 중복 확인 (AJAX)
     */
    @GetMapping("/api/check-email")
    public String checkEmail(@RequestParam String email, Model model) {
        boolean exists = userService.existsByEmail(email);
        model.addAttribute("exists", exists);
        return "jsonView"; // JSON 응답을 위한 뷰 (필요시 별도 처리)
    }

    /**
     * 이메일 인증 번호 발송
     */
    @PostMapping("/api/send-verification-code")
    public String sendVerificationCode(@RequestParam String email, Model model) {
        try {
            userService.generateEmailVerificationCode(email);
            // 실제로는 이메일 발송 로직이 필요함 (현재는 코드만 생성)
            model.addAttribute("success", true);
            model.addAttribute("message", "인증 번호가 발송되었습니다.");
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "인증 번호 발송에 실패했습니다.");
        }
        return "jsonView";
    }

    /**
     * 이메일 인증 번호 검증
     */
    @PostMapping("/api/verify-email-code")
    public String verifyEmailCode(@RequestParam String email, @RequestParam String code, Model model) {
        boolean verified = userService.verifyEmailCode(email, code);
        model.addAttribute("verified", verified);
        return "jsonView";
    }
}

