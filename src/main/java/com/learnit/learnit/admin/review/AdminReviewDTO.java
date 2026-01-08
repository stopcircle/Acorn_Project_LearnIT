package com.learnit.learnit.admin.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReviewDTO {
    private Long reviewId;
    private Integer courseId;
    private String courseTitle;
    private Long userId;
    private String userName;
    private String userEmail;
    private Integer rating;
    private String comment;
    private String commentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
