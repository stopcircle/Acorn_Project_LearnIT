package com.learnit.learnit.Course;

import com.learnit.learnit.Course.Course;
import com.learnit.learnit.Course.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    @GetMapping("/test")
    public List<Course> dbTest() {
        System.out.println("✅ DB 연결 테스트 요청 들어옴!");
        return courseMapper.getTestList();
    }
}