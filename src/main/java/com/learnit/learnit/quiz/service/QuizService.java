package com.learnit.learnit.quiz.service;

import com.learnit.learnit.quiz.dto.Quiz;
import com.learnit.learnit.quiz.repository.QuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizMapper quizMapper;

    // 사이드바용 리스트
    public List<Quiz> getQuizList(Long courseId) {
        return quizMapper.selectQuizListByCourseId(courseId);
    }

    // 퀴즈 상세 조회 (조건 검사 없이 바로 리턴)
    public Quiz getQuiz(Long quizId) {
        return quizMapper.selectQuizByQuizId(quizId);
    }
}