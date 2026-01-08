package com.learnit.learnit.admin.qna;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminQnaDTO {
    // Q&A 기본 정보
    private Integer qnaId;
    private Integer courseId;
    private String courseTitle;
    
    // 질문 정보
    private Long questionUserId;
    private String questionUserName;
    private String questionUserEmail;
    private String questionTitle;
    private String questionContent;
    private LocalDateTime questionCreatedAt;
    private String isResolved;
    
    // 답변 정보
    private Integer answerId;
    private Long answerUserId;
    private String answerUserName;
    private String answerContent;
    private LocalDateTime answerCreatedAt;
    
    // 화면 표시용 (컨트롤러에서 설정)
    private String typeLabel;  // "강의 Q&A" 또는 "전체 Q&A"
    private String statusLabel; // "ACTIVE" 또는 "PASS"
}
