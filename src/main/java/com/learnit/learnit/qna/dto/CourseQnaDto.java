package com.learnit.learnit.qna.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

public class CourseQnaDto {

    @Data
    public static class QuestionCreateReq {
        private Long courseId;
        private String content;
    }

    @Data
    public static class QuestionUpdateReq {
        private String content;
    }

    @Data
    public static class AnswerCreateReq {
        private Long qnaId;
        private Long parentAnswerId; // null이면 답변, 있으면 대댓글
        private String content;
        private String isResolved; // 답변 등록 시 선택(관리자만), 없으면 null
    }

    @Data
    public static class AnswerUpdateReq {
        private String content;
        private String isResolved; // 관리자/서브관리자 답변 수정 시 선택 가능
    }

    @Getter @Setter
    public static class QuestionRes {
        private Long qnaId;
        private Long courseId;
        private Long userId;
        private String writerNickname;
        private String content;
        private String isResolved;
        private String createdAt;
        private String updatedAt;

        // 권한 플래그(프론트 편의)
        // ✅ 수정: 질문 권한 분리
        private boolean canEditQuestion;
        private boolean canDeleteQuestion;
        private boolean canAnswer;
        private boolean canSetResolved;

        private List<AnswerRes> answers = new ArrayList<>();


    }

    @Getter @Setter
    public static class AnswerRes {
        private Long answerId;
        private Long qnaId;
        private Long userId;
        private String writerNickname;
        private Long parentAnswerId; // null이면 상위
        private String content;
        private String createdAt;

        // 권한 플래그
        private boolean canEdit;
        private boolean canDelete;
        private boolean canReply; // 질문자만 답변에 대댓글 가능

        private List<AnswerRes> children = new ArrayList<>();
    }
}
