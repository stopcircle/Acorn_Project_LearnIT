package com.learnit.learnit.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public List<ReviewDTO> getReviewsByCourseId(Long courseId) {
        return reviewRepository.findByCourseIdAndDeleteFlg(courseId, 0);
    }

}