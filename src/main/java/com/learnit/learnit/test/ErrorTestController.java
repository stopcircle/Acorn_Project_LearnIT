package com.learnit.learnit.test;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ErrorTestController {
    @GetMapping("/test/500")
    public String test500() {
        String s = null;
        return s.toString();
    }

    @GetMapping("/test/404")
    public String test404() {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "forced 404 for test"
        );
    }
}