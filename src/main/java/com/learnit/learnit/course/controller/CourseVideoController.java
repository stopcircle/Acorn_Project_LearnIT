package com.learnit.learnit.course.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CourseVideoController {
    @GetMapping("/course/courseId/video")
    public String courseVideo(){
        return "course/courseVideo";
    }
}
