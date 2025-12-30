package com.learnit.learnit.courseDetail;

import com.learnit.learnit.auth.AuthUtil;
import com.learnit.learnit.course.CourseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CourseDetailController {

    private final CourseDetailService courseDetailService;

    @GetMapping("/CourseDetail")
    public String detail(@RequestParam("courseId") int courseId,
                         @RequestParam(value = "tab", defaultValue = "intro") String tab,
                         Model model) {

        try {
            Long userId = AuthUtil.getLoginUserId();   // ✅ 로컬/소셜 통합
            boolean isLoggedIn = (userId != null);
            boolean isEnrolled = isLoggedIn && courseDetailService.isEnrolled(userId, courseId);

            setCommonModel(model, courseId, tab, isLoggedIn, isEnrolled, userId);

            return "courseDetail/CourseDetail";

        } catch (Exception e) {
            // ✅ 콘솔 에러만 찍고 사용자에게는 에러 페이지/메시지로 처리
            e.printStackTrace();

            model.addAttribute("errorMessage", "강의 상세 정보를 불러오는 중 오류가 발생했습니다.");
            return "error/500"; // ❗ error/500.html 없으면 아래 주석처럼 다른 페이지로 변경
            // return "redirect:/";  // 홈으로 보내고 싶으면 이걸 사용
        }
    }

    private void setCommonModel(Model model,
                                int courseId,
                                String tab,
                                boolean isLoggedIn,
                                boolean isEnrolled,
                                Long userId) {

        // ✅ 1) 강의 조회
        CourseDTO course = courseDetailService.getCourse(courseId);
        if (course == null) {
            model.addAttribute("errorMessage", "존재하지 않는 강의입니다.");
            // error/404.html 필요 (없으면 redirect:/ 로 변경 가능)
            throw new IllegalArgumentException("Course not found. courseId=" + courseId);
        }

        // ✅ 2) 기본 모델
        model.addAttribute("course", course);
        model.addAttribute("activeTab", tab);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("loginUserId", userId);

        // ✅ 3) 챕터/커리큘럼 (더미 제거: 없으면 빈 리스트/빈 맵)
        List<ChapterDTO> chapters = courseDetailService.getChapters(courseId);
        model.addAttribute("chapters", chapters);

        Map<String, List<ChapterDTO>> sectionMap = courseDetailService.getCurriculumSectionMap(courseId);
        model.addAttribute("sectionMap", sectionMap);

        model.addAttribute("curriculumTotal", chapters.size());

        // ✅ 4) 리뷰 더미/서비스 제거: 템플릿 안전용 빈 리스트만 내려줌
        model.addAttribute("reviews", Collections.emptyList());
    }
}
