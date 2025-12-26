package com.learnit.learnit.review;
import com.learnit.learnit.mypage.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import java.security.Principal;

//http://localhost:8080/api/reviews?courseId=2

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;

    @GetMapping
    public List<ReviewDTO> getReviews(@RequestParam("courseId") Long courseId) {
        return reviewService.getReviewsByCourseId(courseId);
    }


}