package com.learnit.learnit.admin.qna;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminQnaService {

    private final AdminQnaRepository repo;

    public int getTotalCount(String type, String status, String searchField, String search,
                             Integer searchQnaId, Long instructorUserId) {
        return repo.countQnas(type, status, searchField, search, searchQnaId, instructorUserId);
    }

    public List<AdminQnaDTO> getList(String type, String status, String searchField, String search,
                                     Integer searchQnaId, int offset, int size, Long instructorUserId) {
        List<AdminQnaDTO> list = repo.selectQnas(offset, size, type, status, searchField, search, searchQnaId, instructorUserId);

        for (AdminQnaDTO dto : list) {
            dto.setTypeLabel(dto.getCourseId() != null ? "강의 Q&A" : "전체 Q&A");
            dto.setStatusLabel("Y".equals(dto.getIsResolved()) ? "PASS" : "ACTIVE");

            // ✅ 제목 표시값 생성
            dto.setQuestionTitleDisplay(makeTitle(dto.getQuestionTitle(), dto.getQuestionContent()));
        }
        return list;
    }

    public AdminQnaDTO getDetail(int qnaId) {
        AdminQnaDTO dto = repo.selectQnaDetail(qnaId);
        if (dto != null) {
            dto.setTypeLabel(dto.getCourseId() != null ? "강의 Q&A" : "전체 Q&A");
            dto.setStatusLabel("Y".equals(dto.getIsResolved()) ? "PASS" : "ACTIVE");
            dto.setQuestionTitleDisplay(makeTitle(dto.getQuestionTitle(), dto.getQuestionContent()));
        }
        return dto;
    }

    public Long getInstructorUserIdByCourseId(Integer courseId) {
        if (courseId == null) return null;
        return repo.selectInstructorUserIdByCourseId(courseId);
    }

    @Transactional
    public void saveAnswerAndStatus(int qnaId, long adminUserId, String content, String newStatus) {
        // ✅ 관리자/서브관리자 답변만 "수정 대상"으로 잡기
        Integer answerId = repo.selectLatestStaffAnswerId(qnaId);

        if (content != null) {
            String trimmed = content.trim();
            if (!trimmed.isEmpty()) {
                if (answerId == null) repo.insertAnswer(qnaId, adminUserId, trimmed);
                else repo.updateAnswer(answerId, trimmed);
            }
        }

        if ("PASS".equalsIgnoreCase(newStatus)) repo.updateResolved(qnaId, "Y");
        else if ("ACTIVE".equalsIgnoreCase(newStatus)) repo.updateResolved(qnaId, "N");
    }

    @Transactional
    public void delete(int qnaId) {
        repo.softDeleteAnswersByQnaId(qnaId);
        repo.softDeleteQuestionById(qnaId);
    }

    // ✅ 제목 없으면 질문 내용 앞부분 요약으로 대체
    private String makeTitle(String title, String content) {
        String t = (title == null) ? "" : title.trim();
        if (!t.isEmpty()) return t;

        String c = (content == null) ? "" : content.trim();
        if (c.isEmpty()) return "질문";

        int max = 18; // 원하는 길이로 조절
        if (c.length() <= max) return c;
        return c.substring(0, max) + "…";
    }
}
