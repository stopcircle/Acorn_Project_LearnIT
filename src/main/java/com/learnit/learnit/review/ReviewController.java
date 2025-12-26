package com.learnit.learnit.review;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    @GetMapping("/review")
    public String showReviewForm(
            @RequestParam Long courseId,
            HttpSession session,
            Model model
    ) {
//        // 로그인 여부 간단 체크 (비로그인이면 로그인 페이지로)
//        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");
//        if (userId == null) {
//            return "redirect:/login";
//        }

        model.addAttribute("courseId", courseId);

        // 👉 templates/test_review/review.html
        return "test_review/review";
    }
}