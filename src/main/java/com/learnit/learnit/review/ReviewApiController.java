package com.learnit.learnit.review;

import com.learnit.learnit.user.User;
import com.learnit.learnit.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    // 목록 조회 + 닉네임 포함
    @GetMapping
    public List<Map<String, Object>> getReviews(@RequestParam("courseId") Long courseId) {

        List<ReviewDTO> reviews = reviewService.getReviewsByCourseId(courseId);

        if (reviews.isEmpty()) return List.of();

        Set<Long> userIds = reviews.stream()
                .map(ReviewDTO::getUserId)
                .collect(Collectors.toSet());

        List<User> users = userRepository.findAllById(userIds);

        Map<Long, String> nicknameMap = users.stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        User::getNickname
                ));

        return reviews.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reviewId", r.getReviewId());
                    map.put("courseId", r.getCourseId());
                    map.put("userId", r.getUserId());
                    map.put("rating", r.getRating());
                    map.put("content", r.getContent());
                    map.put("createdAt", r.getCreatedAt());
                    map.put("updatedAt", r.getUpdatedAt());
                    map.put("nickname",
                            nicknameMap.getOrDefault(r.getUserId(), "익명"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    // 등록
    @PostMapping
    public ReviewDTO createReview(
            @RequestParam Long courseId,
            @RequestBody ReviewDTO review,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");
        if (userId == null) throw new RuntimeException("로그인 후 이용 가능합니다.");

        return reviewService.createReview(courseId, userId, review);
    }

    // 수정
    @PutMapping("/{reviewId}")
    public ReviewDTO updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewDTO review,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");
        if (userId == null) throw new RuntimeException("로그인 후 이용 가능합니다.");

        return reviewService.updateReview(reviewId, userId, review);
    }

    // 삭제 (소프트삭제)
    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @PathVariable Long reviewId,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");
        if (userId == null) throw new RuntimeException("로그인 후 이용 가능합니다.");

        reviewService.deleteReview(reviewId, userId);
    }

    /* 로그인 + 수강 여부 체크 (리뷰 폼 노출용) */
    @GetMapping("/check-enrollment")
    public Map<String, Boolean> checkEnrollment(
            @RequestParam Long courseId,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        Map<String, Boolean> result = new HashMap<>();

        if (userId == null) {
            result.put("loggedIn", false);
            result.put("enrolled", false);
            return result;
        }

        boolean enrolled = reviewService.isEnrolledUser(courseId, userId); // ⭐ 서비스 통해 체크

        result.put("loggedIn", true);
        result.put("enrolled", enrolled);
        return result;
    }

}
