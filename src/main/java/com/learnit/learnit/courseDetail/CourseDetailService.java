package com.learnit.learnit.courseDetail;

import com.learnit.learnit.course.CourseDTO;
import com.learnit.learnit.enroll.EnrollmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseDetailService {

    private final CourseDetailMapper courseDetailMapper;
    private final EnrollmentMapper enrollmentMapper;

    public CourseDTO getCourse(int courseId) {
        return courseDetailMapper.selectCourseDetail(courseId);
    }

    public boolean isEnrolled(Long userId, int courseId) {
        return enrollmentMapper.countEnrollment(userId, courseId) > 0;
    }

    /**
     * ✅ 더미 제거: DB에서만 조회
     * - 없으면 빈 리스트 반환
     */
    public List<ChapterDTO> getChapters(int courseId) {
        List<ChapterDTO> list = courseDetailMapper.selectChaptersByCourseId(courseId);
        return (list == null) ? Collections.emptyList() : list;
    }

    /**
     * ✅ 섹션별 그룹핑 Map (DB 데이터 기반)
     * - section_title 없으면 "섹션 1. 커리큘럼"으로 묶음
     * - 챕터가 없으면 빈 Map 반환
     */
    public Map<String, List<ChapterDTO>> getCurriculumSectionMap(int courseId) {
        List<ChapterDTO> chapters = getChapters(courseId);

        if (chapters.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<ChapterDTO>> sectionMap = new LinkedHashMap<>();
        for (ChapterDTO ch : chapters) {
            String key = ch.getSectionTitle();
            if (key == null || key.isBlank()) key = "섹션 1. 커리큘럼";
            sectionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(ch);
        }
        return sectionMap;
    }

    public int getCurriculumTotalCount(int courseId) {
        return getChapters(courseId).size();
    }
}
