package com.learnit.learnit.review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewDTO, Long> {

    List<ReviewDTO> findByCourseIdAndDeleteFlg(Long courseId, Integer deleteFlg);
}