package com.learnit.learnit.course;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CourseDetailController {

    private final CourseMapper courseMapper;

    // ✅ 기본 진입: /CourseDetail?courseId=1 → 소개 페이지로 렌더
    @GetMapping("/CourseDetail")
    public String detail(@RequestParam("courseId") int courseId, Model model) {
        return intro(courseId, model);
    }

    @GetMapping("/CourseDetail/intro")
    public String intro(@RequestParam("courseId") int courseId, Model model) {
        CourseDTO course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            model.addAttribute("message", "해당 강의를 찾을 수 없습니다.");
            return "courseDetail/not-found";
        }

        putCommon(model, course, "intro");
        return "courseDetail/intro";
    }

    @GetMapping("/CourseDetail/curriculum")
    public String curriculum(@RequestParam("courseId") int courseId, Model model) {
        CourseDTO course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            model.addAttribute("message", "해당 강의를 찾을 수 없습니다.");
            return "courseDetail/not-found";
        }

        putCommon(model, course, "curriculum");

        // ✅ 커리큘럼은 DB 아직 없으니까 더미로 3개만
        model.addAttribute("curriculum", List.of(
                new CurriculumItem("섹션 1. 오리엔테이션", "강의 소개 및 준비 환경 세팅"),
                new CurriculumItem("섹션 2. 핵심 개념", "RAG / Vector DB / Retrieval 개념"),
                new CurriculumItem("섹션 3. 실습", "Spring AI로 챗봇 구현 실습")
        ));

        return "courseDetail/curriculum";
    }

    @GetMapping("/CourseDetail/reviews")
    public String reviews(@RequestParam("courseId") int courseId, Model model) {
        CourseDTO course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            model.addAttribute("message", "해당 강의를 찾을 수 없습니다.");
            return "courseDetail/not-found";
        }

        putCommon(model, course, "reviews");

        // ✅ 수강평 3개만 구현(더미)
        model.addAttribute("reviews", List.of(
                new ReviewItem("dmax", 5.0, "많은 도움이 되었습니다. 고맙습니다!"),
                new ReviewItem("Jang Jaehoon", 5.0, "좋은 강의 감사합니다!"),
                new ReviewItem("masiljangajji", 5.0, "Good")
        ));

        return "courseDetail/reviews";
    }

    // -----------------------
    // 공통 데이터 세팅
    // -----------------------
    private void putCommon(Model model, CourseDTO course, String activeTab) {
        model.addAttribute("course", course);
        model.addAttribute("activeTab", activeTab);

        // ✅ 오른쪽 카드/상단 메타 더미
        model.addAttribute("ratingAvg", 4.9);
        model.addAttribute("reviewCount", 27);
        model.addAttribute("studentCount", 1750);
        model.addAttribute("instructorName", "Sonic AI");
        model.addAttribute("tags", List.of("Spring AI", "LLM", "RAG", "Spring", "Spring Boot"));
        model.addAttribute("lectureCount", 8);
        model.addAttribute("lectureTime", "2시간 49분");
        model.addAttribute("levelText", "입문 · 초급 · 중급이상");
    }

    public record CurriculumItem(String title, String desc) {}
    public record ReviewItem(String writer, double rating, String content) {}
}
