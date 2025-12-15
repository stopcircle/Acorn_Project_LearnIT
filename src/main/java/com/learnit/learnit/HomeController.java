package com.learnit.learnit;

import com.learnit.learnit.home.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/home")
    public String home(Model model) {

        model.addAttribute("bannerList", homeService.getBannerCourse());
        model.addAttribute("courseList", homeService.getPopularCourseList());

        return "home/mainContent";
    }
}
