package com.learnit.learnit.qna.dto;

import lombok.Data;

import java.util.List;

public class CourseQnaDTO {

    @Data
    public static class QuestionRes {
        private Long qnaId;
        private Long courseId;
        private Long userId;
        private String writerNickname;
        private String content;
        private String isResolved;
        private String createdAt;
        private String updatedAt;
        
        // 권한 플래그 (Service에서 설정)
        private boolean canEditQuestion;
        private boolean canDeleteQuestion;
        private boolean canAnswer;
        private boolean canComment;
        private boolean canSetResolved;
        
        // 답변 목록
        private List<AnswerRes> answers;
    }

    @Data
    public static class AnswerRes {
        private Long answerId;
        private Long qnaId;
        private Long userId;
        private String writerNickname;
        private Long parentAnswerId;
        private String content;
        private String createdAt;
        
        // 권한 플래그 (Service에서 설정)
        private boolean canEdit;
        private boolean canDelete;
        private boolean canReply;
        private boolean comment; // 댓글/답변 구분 플래그
        private List<AnswerRes> children; // 미사용 (빈 리스트)
    }

    @Data
    public static class QuestionCreateReq {
        private Long courseId;
        private String content;
    }

    @Data
    public static class QuestionUpdateReq {
        private Long qnaId;
        private String content;
    }

    @Data
    public static class AnswerCreateReq {
        private Long qnaId;
        private Long parentAnswerId;
        private String content;
        private String isResolved; // 관리자만 사용
    }

    @Data
    public static class AnswerUpdateReq {
        private Long answerId;
        private String content;
        private String isResolved; // 관리자만 사용
    }
}
