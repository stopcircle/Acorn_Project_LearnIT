package com.learnit.learnit.review;

import com.learnit.learnit.enrollment.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        // 수강 여부 확인
        boolean enrolled = enrollmentRepository
                .existsByUserIdAndCourseIdAndStatus(userId, courseId, "ACTIVE");

        if (!enrolled) {
            throw new IllegalStateException("수강 중인 사용자만 리뷰를 작성할 수 있습니다.");
        }

        // 이미 리뷰 있으면 막기
        if (reviewRepository.existsByCourseIdAndUserId(courseId, userId)) {
            throw new IllegalStateException("이미 이 강의에 수강평이 등록되었습니다.");
        }

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
}
