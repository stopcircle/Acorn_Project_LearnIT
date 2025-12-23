package com.learnit.learnit.courseDetail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CourseDetailController {

    private final CourseDetailService courseDetailService;

    // 강의 소개
    @GetMapping("/CourseDetail/intro")
    public String intro(@RequestParam("courseId") int courseId, Model model) {
        model.addAttribute("course", courseDetailService.getCourse(courseId));
        model.addAttribute("activeTab", "intro");
        return "courseDetail/intro";
    }

    // 커리큘럼
    @GetMapping("/CourseDetail/curriculum")
    public String curriculum(@RequestParam("courseId") int courseId, Model model) {
        model.addAttribute("course", courseDetailService.getCourse(courseId));
        model.addAttribute("activeTab", "curriculum");
        return "courseDetail/curriculum";
    }

    // 수강평
    @GetMapping("/CourseDetail/reviews")
    public String reviews(@RequestParam("courseId") int courseId, Model model) {
        model.addAttribute("course", courseDetailService.getCourse(courseId));
        model.addAttribute("activeTab", "reviews");

        // ✅ 일단 3개 고정 더미 (DB 없으면 임시)
        model.addAttribute("reviews", courseDetailService.getDummyReviews());

        return "courseDetail/reviews";
    }
}
