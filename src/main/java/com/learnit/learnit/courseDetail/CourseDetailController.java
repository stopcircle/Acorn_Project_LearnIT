package com.learnit.learnit.courseDetail;

import com.learnit.learnit.course.CourseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class CourseDetailController {

    private final CourseDetailService courseDetailService;

    // ✅ 1) 비로그인 상세
    @GetMapping("/CourseDetail")
    public String detail(@RequestParam("courseId") int courseId,
                         @RequestParam(value = "tab", defaultValue = "intro") String tab,
                         Model model) {

        setCommonModel(model, courseId, tab, false, false);
        return "courseDetail/courseDetail.html";
    }

    // ✅ 2) 로그인 상세 (미수강이면 enroll+cart / 수강중이면 study로)
    @GetMapping("/CourseDetailLogin")
    public String detailLogin(@RequestParam("courseId") int courseId,
                              @RequestParam(value = "tab", defaultValue = "intro") String tab,
                              Model model) {

        Long userId = 5L; // ✅ 임시 고정

        // ✅ 수강중이면 3번 상태로 보내기
        if (courseDetailService.isEnrolled(userId, courseId)) {
            return "redirect:/CourseDetailStudy?courseId=" + courseId + "&tab=" + tab;
        }

        // ✅ 로그인 + 미수강
        setCommonModel(model, courseId, tab, true, false);
        return "courseDetail/courseDetail.html";
    }

    // ✅ 3) 로그인 + 수강중 상세 (이어보기만)
    @GetMapping("/CourseDetailStudy")
    public String detailStudy(@RequestParam("courseId") int courseId,
                              @RequestParam(value = "tab", defaultValue = "intro") String tab,
                              Model model) {

        Long userId = 5L; // ✅ 임시 고정

        // ✅ 수강중 아니면 로그인 상세로 되돌림
        if (!courseDetailService.isEnrolled(userId, courseId)) {
            return "redirect:/CourseDetailLogin?courseId=" + courseId + "&tab=" + tab;
        }

        // ✅ 로그인 + 수강중
        setCommonModel(model, courseId, tab, true, true);
        return "courseDetail/courseDetailStudy.html";
    }

    private void setCommonModel(Model model, int courseId, String tab,
                                boolean isLoggedIn, boolean isEnrolled) {

        CourseDTO course = courseDetailService.getCourse(courseId);

        model.addAttribute("course", course);
        model.addAttribute("activeTab", tab);

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("isEnrolled", isEnrolled); // ✅ 핵심 플래그

        model.addAttribute("userName", isLoggedIn ? "user" : null);

        model.addAttribute("chapters", courseDetailService.getChaptersOrDummy(courseId));

        if ("reviews".equals(tab)) {
            model.addAttribute("reviews", courseDetailService.getDummyReviews());
        } else {
            model.addAttribute("reviews", Collections.emptyList());
        }
    }
}
