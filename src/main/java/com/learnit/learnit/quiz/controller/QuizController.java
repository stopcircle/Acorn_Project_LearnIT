package com.learnit.learnit.quiz.controller;

import com.learnit.learnit.quiz.dto.Quiz;
import com.learnit.learnit.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    // JS에서 'chapterId'라는 파라미터 이름으로 보내지만, 실제 값은 quizId입니다.
    // 헷갈림 방지를 위해 변수는 받아주되 서비스엔 quizId로 넘깁니다.
    public ResponseEntity<?> getQuiz(@RequestParam("chapterId") Long quizId) {

        Quiz quiz = quizService.getQuiz(quizId);

        if (quiz == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(quiz);
    }
}