package com.learnit.learnit.courseDetail;

import com.learnit.learnit.course.CourseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseDetailService {

    private final CourseDetailMapper courseDetailMapper;

    public CourseDTO getCourse(int courseId) {
        return courseDetailMapper.selectCourseDetail(courseId);
    }

    // ✅ 수강평 3개만 임시 구현
    public List<Map<String, Object>> getDummyReviews() {
        return List.of(
                Map.of("name", "dmax", "rating", 5.0, "comment", "많은 도움이 되었습니다. 고맙습니다!"),
                Map.of("name", "Jang Jaehoon", "rating", 5.0, "comment", "좋은 강의 감사합니다!"),
                Map.of("name", "masiljangajji", "rating", 5.0, "comment", "Good")
        );
    }
}
