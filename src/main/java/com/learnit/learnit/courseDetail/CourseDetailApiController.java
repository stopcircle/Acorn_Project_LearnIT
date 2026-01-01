package com.learnit.learnit.courseDetail;

import com.learnit.learnit.auth.AuthUtil;
import com.learnit.learnit.user.entity.User;
import com.learnit.learnit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class CourseDetailApiController {

    private final CourseDetailService courseDetailService;
    private final UserRepository userRepository;

    // ================================
    // 목록 조회 + 닉네임 포함
    // ================================
    @GetMapping
    public List<Map<String, Object>> getReviews(@RequestParam("courseId") Long courseId) {

        Long loginUserId = AuthUtil.getLoginUserId();

        List<ReviewDTO> reviews = courseDetailService.getReviewsByCourseId(courseId);
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
                    map.put("nickname", nicknameMap.getOrDefault(r.getUserId(), "익명"));
                    map.put("mine", loginUserId != null && loginUserId.equals(r.getUserId()));
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ================================
    // 평점 요약 (평균 + 개수)
    // ================================
    @GetMapping("/summary")
    public Map<String, Object> getReviewSummary(@RequestParam Long courseId) {
        return courseDetailService.getReviewSummary(courseId);
    }

    // ================================
    // 등록
    // ================================
    @PostMapping
    public ReviewDTO createReview(
            @RequestParam Long courseId,
            @RequestBody ReviewDTO review
    ) {
        Long userId = AuthUtil.getLoginUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 후 이용 가능합니다.");
        }
        return courseDetailService.createReview(courseId, userId, review);
    }

    // ================================
    // 수정
    // ================================
    @PutMapping("/{reviewId}")
    public ReviewDTO updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewDTO review
    ) {
        Long userId = AuthUtil.getLoginUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 후 이용 가능합니다.");
        }
        return courseDetailService.updateReview(reviewId, userId, review);
    }

    // ================================
    // 삭제 (소프트 삭제: delete_flg=1)
    // ================================
    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable Long reviewId) {

        Long userId = AuthUtil.getLoginUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 후 이용 가능합니다.");
        }

        courseDetailService.deleteReview(reviewId, userId);
    }

    // ================================
    // 리뷰 작성 가능 여부 체크
    //  - loggedIn: 로그인 여부
    //  - enrolled: 수강 여부
    //  - canWriteReview: (로그인 + 수강) && (delete_flg=0 인 내 리뷰가 없음)
    // ================================
    @GetMapping("/check-enrollment")
    public Map<String, Boolean> checkEnrollment(@RequestParam Long courseId) {

        Long userId = AuthUtil.getLoginUserId();
        Map<String, Boolean> result = new HashMap<>();

        if (userId == null) {
            result.put("loggedIn", false);
            result.put("enrolled", false);
            result.put("canWriteReview", false);
            return result;
        }

        boolean enrolled = courseDetailService.isEnrolledUser(courseId, userId);
        boolean canWriteReview = courseDetailService.canWriteReview(courseId, userId);

        result.put("loggedIn", true);
        result.put("enrolled", enrolled);
        result.put("canWriteReview", canWriteReview);

        return result;
    }
}
