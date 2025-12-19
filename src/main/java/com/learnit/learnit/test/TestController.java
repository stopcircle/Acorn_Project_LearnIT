package com.learnit.learnit.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    // http://localhost:8080/test 로 접속하면 화면만 나오는 단순 페이지
    @GetMapping("/test")
    public String testPage() {
        // templates/test/testPage.html 을 바라봄
        return "test/testPage";
    }
}