package com.learnit.learnit.review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewDTO, Long> {

    // 목록 조회: delete_flg=0 & comment_status != 'REJECTED'
    List<ReviewDTO> findByCourseIdAndDeleteFlg(Long courseId, Integer deleteFlg);

    // 한 강의에 한 유저가 리뷰 썼는지 여부 (중복 방지)
    boolean existsByCourseIdAndUserId(Long courseId, Long userId);
}
