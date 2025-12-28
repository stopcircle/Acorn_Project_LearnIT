package com.learnit.learnit.quiz.dto;

import lombok.Data;
import java.util.List;

@Data
public class Quiz {
    private Long quizId;
    private String title;         // 퀴즈 제목
    private String sectionTitle;  // 섹션 제목 (어느 섹션의 퀴즈인지 구분용)
    private List<Question> questions;
}