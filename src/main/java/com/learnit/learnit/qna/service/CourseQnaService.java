package com.learnit.learnit.qna.service;

import com.learnit.learnit.qna.dto.CourseQnaDto;
import com.learnit.learnit.qna.mapper.CourseQnaMapper;
import com.learnit.learnit.user.util.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("courseQnaService")
@RequiredArgsConstructor
public class CourseQnaService {

    private final CourseQnaMapper courseQnaMapper;

    private boolean isEnrolledActive(Long userId, Long courseId) {
        String status = courseQnaMapper.selectEnrollmentStatus(userId, courseId);
        return "ACTIVE".equals(status);
    }

    private boolean isAdmin(Long userId) {
        return courseQnaMapper.isAdmin(userId) > 0;
    }

    private boolean isSubAdminOfCourse(Long userId, Long courseId) {
        return courseQnaMapper.isSubAdminOfCourse(userId, courseId) > 0;
    }

    private boolean canAnswer(Long userId, Long courseId) {
        return isAdmin(userId) || isSubAdminOfCourse(userId, courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseQnaDto.QuestionRes> getQuestions(Long courseId) {
        Long loginUserId = SessionUtils.getLoginUserId();
        boolean isAdmin = (loginUserId != null && isAdmin(loginUserId));
        boolean isSubAdmin = (loginUserId != null && isSubAdminOfCourse(loginUserId, courseId));
        boolean canAnswer = (loginUserId != null && (isAdmin || isSubAdmin));

        List<CourseQnaDto.QuestionRes> questions = courseQnaMapper.selectQuestions(courseId);
        List<CourseQnaDto.AnswerRes> answers = courseQnaMapper.selectAnswers(courseId);

        // 답변을 qnaId 기준 그룹핑
        Map<Long, List<CourseQnaDto.AnswerRes>> byQna = answers.stream()
                .collect(Collectors.groupingBy(CourseQnaDto.AnswerRes::getQnaId));

        for (CourseQnaDto.QuestionRes q : questions) {
            Long ownerId = q.getUserId();

            boolean isOwner = (loginUserId != null && Objects.equals(loginUserId, ownerId));

// ✅ 수정: 질문 수정은 작성자만
            q.setCanEditQuestion(isOwner);

// ✅ 수정: 질문 삭제는 작성자 + 관리자(전체/강의)
            q.setCanDeleteQuestion(isOwner || isAdmin || isSubAdmin);
            q.setCanAnswer(canAnswer);
            q.setCanSetResolved(canAnswer); // 답변 등록/수정 시 해결여부 선택 가능

            // 답변 트리 구성(부모=null은 상위, 나머지는 children)
            List<CourseQnaDto.AnswerRes> list = byQna.getOrDefault(q.getQnaId(), Collections.emptyList());

            Map<Long, CourseQnaDto.AnswerRes> map = new LinkedHashMap<>();
            List<CourseQnaDto.AnswerRes> top = new ArrayList<>();

            for (CourseQnaDto.AnswerRes a : list) {
                map.put(a.getAnswerId(), a);
                a.setChildren(new ArrayList<>());
            }

            for (CourseQnaDto.AnswerRes a : list) {
                // 권한 플래그
                // - canEdit: 작성자만 true (어드민으로 로그인해도 타인글 수정 버튼 X)
                // - canDelete: 관리자(전체/강의)만 true  (대댓글도 동일 적용)
                // - canReply: 질문자만 답변에 대댓글 가능
                boolean isWriter = (loginUserId != null && Objects.equals(loginUserId, a.getUserId())); // ✅ 수정
                boolean canEdit = isWriter;                                                            // ✅ 수정

                boolean canDelete = (loginUserId != null && (isAdmin || isSubAdmin));                   // (그대로 OK)
                boolean canReply = (loginUserId != null && Objects.equals(loginUserId, ownerId));       // (그대로 OK)

                a.setCanEdit(canEdit);
                a.setCanDelete(canDelete);
                a.setCanReply(canReply);

                if (a.getParentAnswerId() == null) {
                    top.add(a);
                } else {
                    CourseQnaDto.AnswerRes parent = map.get(a.getParentAnswerId());
                    if (parent != null) parent.getChildren().add(a);
                    else top.add(a); // 예외 대비
                }
            }

            q.setAnswers(top);
        }

        return questions;
    }

    @Transactional
    public void createQuestion(Long courseId, String content) {
        Long userId = SessionUtils.requireLoginUserId();

        if (!isEnrolledActive(userId, courseId)) {
            throw new SecurityException("수강중인 회원만 질문 가능");
        }
        courseQnaMapper.insertQuestion(courseId, userId, content);
    }

    @Transactional
    public void updateQuestion(Long qnaId, String content) {
        Long userId = SessionUtils.requireLoginUserId();

        int updated = courseQnaMapper.updateQuestionByOwner(qnaId, userId, content);
        if (updated == 0) {
            throw new SecurityException("본인 질문만 수정 가능");
        }
    }

    @Transactional
    public void deleteQuestion(Long qnaId, Long courseId) {
        Long userId = SessionUtils.requireLoginUserId();

        Long ownerId = courseQnaMapper.selectQuestionOwnerId(qnaId);
        boolean allow = Objects.equals(userId, ownerId) || isAdmin(userId) || isSubAdminOfCourse(userId, courseId);

        if (!allow) throw new SecurityException("삭제 권한 없음");

        // 질문 delete_flg=1 => 답변도 조회에서 같이 숨김
        courseQnaMapper.softDeleteQuestion(qnaId);
    }

    @Transactional
    public void createAnswer(CourseQnaDto.AnswerCreateReq req, Long courseId) {
        Long userId = SessionUtils.requireLoginUserId();

        // 답변(상위): 관리자만
        // 대댓글(답변에 대한 댓글): 질문자만
        if (req.getParentAnswerId() == null) {
            if (!canAnswer(userId, courseId)) throw new SecurityException("답변 권한 없음");
        } else {
            Long ownerId = courseQnaMapper.selectQuestionOwnerId(req.getQnaId());
            if (!Objects.equals(ownerId, userId)) throw new SecurityException("대댓글은 질문자만 가능");
        }

        courseQnaMapper.insertAnswer(req.getQnaId(), userId, req.getParentAnswerId(), req.getContent());

        // 답변 등록 시 해결여부 선택값이 오면 반영(관리자만)
        if (req.getParentAnswerId() == null && req.getIsResolved() != null && canAnswer(userId, courseId)) {
            courseQnaMapper.updateQuestionResolved(req.getQnaId(), req.getIsResolved());
        }

        // 질문자가 답변에 대댓글 달면 is_resolved는 자동 N
        if (req.getParentAnswerId() != null) {
            courseQnaMapper.updateQuestionResolved(req.getQnaId(), "N");
        }
    }

    @Transactional
    public void updateAnswer(Long answerId, CourseQnaDto.AnswerUpdateReq req, Long courseId) {
        Long userId = SessionUtils.requireLoginUserId();

        if (!canAnswer(userId, courseId)) throw new SecurityException("수정 권한 없음");

        courseQnaMapper.updateAnswer(answerId, req.getContent());

        // 해결여부 변경은 질문에 반영
        if (req.getIsResolved() != null) {
            Long qnaId = courseQnaMapper.selectAnswerQnaId(answerId);
            courseQnaMapper.updateQuestionResolved(qnaId, req.getIsResolved());
        }
    }

    @Transactional
    public void deleteAnswer(Long answerId, Long courseId) {
        Long userId = SessionUtils.requireLoginUserId();

        if (!canAnswer(userId, courseId)) throw new SecurityException("삭제 권한 없음");

        courseQnaMapper.softDeleteAnswer(answerId);
    }
}
