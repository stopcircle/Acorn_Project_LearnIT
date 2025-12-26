package com.learnit.learnit.review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewDTO, Long> {

    // 목록용: 삭제 안 된 것만
    List<ReviewDTO> findByCourseIdAndDeleteFlg(Long courseId, Integer deleteFlg);

    // 한 유저가 한 강의에 쓴 리뷰 1개 (삭제 여부 포함 전체)
    Optional<ReviewDTO> findByCourseIdAndUserId(Long courseId, Long userId);
}
