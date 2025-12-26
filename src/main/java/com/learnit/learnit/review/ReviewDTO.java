package com.learnit.learnit.review;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String content;

    private Integer deleteFlg;

    private String commentStatus;

    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;
}