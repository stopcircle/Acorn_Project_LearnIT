package com.learnit.learnit.admin.qna;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminQnaService {

    private final AdminQnaRepository repo;

    public int getTotalCount(String type, String status, String searchField, String search, Integer searchQnaId, Long instructorUserId) {
        return repo.countQnas(type, status, searchField, search, searchQnaId, instructorUserId);
    }

    public List<AdminQnaDTO> getList(String type, String status, String searchField, String search,
                                     Integer searchQnaId, int offset, int size, Long instructorUserId) {
        List<AdminQnaDTO> list = repo.selectQnas(offset, size, type, status, searchField, search, searchQnaId, instructorUserId);
        // typeLabel과 statusLabel 설정
        for (AdminQnaDTO dto : list) {
            // typeLabel 설정
            if (dto.getCourseId() != null) {
                dto.setTypeLabel("강의 Q&A");
            } else {
                dto.setTypeLabel("전체 Q&A");
            }
            // statusLabel 설정
            if ("Y".equals(dto.getIsResolved())) {
                dto.setStatusLabel("PASS");
            } else {
                dto.setStatusLabel("ACTIVE");
            }
        }
        return list;
    }

    public AdminQnaDTO getDetail(int qnaId) {
        AdminQnaDTO dto = repo.selectQnaDetail(qnaId);
        if (dto != null) {
            // typeLabel과 statusLabel 설정
            if (dto.getCourseId() != null) {
                dto.setTypeLabel("강의 Q&A");
            } else {
                dto.setTypeLabel("전체 Q&A");
            }
            // statusLabel 설정
            if ("Y".equals(dto.getIsResolved())) {
                dto.setStatusLabel("PASS");
            } else {
                dto.setStatusLabel("ACTIVE");
            }
        }
        return dto;
    }

    public Long getInstructorUserIdByCourseId(Integer courseId) {
        if (courseId == null) return null;
        return repo.selectInstructorUserIdByCourseId(courseId);
    }

    @Transactional
    public void saveAnswerAndStatus(int qnaId, long adminUserId, String content, String newStatus) {
        Integer answerId = repo.selectLatestAnswerId(qnaId);

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
}
