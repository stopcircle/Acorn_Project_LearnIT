package com.learnit.learnit.admin.qna;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminQnaRepository {

    List<AdminQnaDto> selectQnas(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("type") String type,        // LECTURE / SITE / null
            @Param("status") String status,    // ACTIVE / PASS / null
            @Param("search") String search,
            @Param("qnaId") Integer qnaId
    );

    int countQnas(
            @Param("type") String type,
            @Param("status") String status,
            @Param("search") String search,
            @Param("qnaId") Integer qnaId
    );

    AdminQnaDto selectQnaDetail(@Param("qnaId") int qnaId);

    Integer selectLatestAnswerId(@Param("qnaId") int qnaId);

    void insertAnswer(
            @Param("qnaId") int qnaId,
            @Param("userId") long userId,
            @Param("content") String content
    );

    void updateAnswer(
            @Param("answerId") int answerId,
            @Param("content") String content
    );

    void updateResolved(
            @Param("qnaId") int qnaId,
            @Param("isResolved") String isResolved
    );

    void softDeleteAnswersByQnaId(@Param("qnaId") int qnaId);

    void softDeleteQuestionById(@Param("qnaId") int qnaId);

    // ✅ qnaId 드롭다운용 (조건에 맞는 qna_id 목록)
    List<Integer> selectQnaIds(
            @Param("type") String type,
            @Param("status") String status,
            @Param("search") String search
    );
}
