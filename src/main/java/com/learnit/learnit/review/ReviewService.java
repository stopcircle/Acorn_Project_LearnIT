package com.learnit.learnit.review;

import com.learnit.learnit.enrollment.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<ReviewDTO> getReviewsByCourseId(Long courseId) {
        List<ReviewDTO> list = reviewRepository.findByCourseIdAndDeleteFlg(courseId, 0);

        // comment_status가 null 이거나 'REJECTED'가 아닌 것만 노출
        return list.stream()
                .filter(r -> !"REJECTED".equals(r.getCommentStatus()))
                .toList();
    }

    @Transactional
    public ReviewDTO createReview(Long courseId, Long userId, ReviewDTO input) {

        // 0. 수강 여부 체크
        boolean enrolled = enrollmentRepository
                .existsByUserIdAndCourseIdAndStatus(userId, courseId, "ACTIVE");

        if (!enrolled) {
            throw new IllegalStateException("수강 중인 사용자만 리뷰를 작성할 수 있습니다.");
        }

        // 1. 이 유저가 이 강의에 쓴 리뷰가 있는지 (삭제 포함 전체 조회)
        Optional<ReviewDTO> optional = reviewRepository.findByCourseIdAndUserId(courseId, userId);

        if (optional.isPresent()) {
            ReviewDTO existing = optional.get();

            // 현재 "살아 있는" 리뷰인지 판별
            boolean isActive =
                    (existing.getDeleteFlg() == null || existing.getDeleteFlg() == 0)
                            && !"REJECTED".equals(existing.getCommentStatus());

            if (isActive) {
                // 👉 이미 등록된, 화면에 보이는 수강평이 있는 경우 → 막기
                throw new IllegalStateException("이미 이 강의에 수강평이 등록되었습니다.");
            }

            // 👉 여기로 온 경우 = 삭제되었거나(REJECTED) 숨겨진 리뷰가 있는 상태
            //    → 같은 row 를 "다시 살리면서" 새로운 내용으로 덮어쓰기
            existing.setRating(input.getRating());
            existing.setContent(input.getContent());
            existing.setDeleteFlg(0);               // 다시 활성화
            existing.setCommentStatus("VISIBLE");   // 다시 노출

            return reviewRepository.save(existing); // UPDATE
        }

        // 2. 아예 처음 작성하는 경우 → 새로 INSERT
        ReviewDTO review = ReviewDTO.builder()
                .courseId(courseId)
                .userId(userId)
                .rating(input.getRating())
                .content(input.getContent())
                .deleteFlg(0)
                .commentStatus("VISIBLE")
                .build();

        return reviewRepository.save(review);
    }

    @Transactional
    public ReviewDTO updateReview(Long reviewId, Long userId, ReviewDTO input) {

        ReviewDTO review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.setRating(input.getRating());
        review.setContent(input.getContent());

        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {

        ReviewDTO review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        review.setDeleteFlg(1);
        //review.setCommentStatus("REJECTED");

        reviewRepository.save(review);
    }
    /**
     * 해당 userId가 courseId 강의를 수강 중인지 여부
     */
    @Transactional(readOnly = true)
    public boolean isEnrolledUser(Long courseId, Long userId) {
        if (userId == null) return false;

        return enrollmentRepository
                .existsByUserIdAndCourseIdAndStatus(userId, courseId, "ACTIVE");
    }
}
