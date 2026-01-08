package com.learnit.learnit.qna.mapper;

import com.learnit.learnit.qna.dto.CourseQnaDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseQnaMapper {

    // 권한
    String selectEnrollmentStatus(@Param("userId") Long userId, @Param("courseId") Long courseId);
    int isAdmin(@Param("userId") Long userId); // ADMIN
    int isSubAdminOfCourse(@Param("userId") Long userId, @Param("courseId") Long courseId); // SUB_ADMIN

    // 질문
    List<CourseQnaDTO.QuestionRes> selectQuestions(@Param("courseId") Long courseId);
    Long selectQuestionOwnerId(@Param("qnaId") Long qnaId);

    int insertQuestion(@Param("courseId") Long courseId, @Param("userId") Long userId, @Param("content") String content);
    int updateQuestionByOwner(@Param("qnaId") Long qnaId, @Param("userId") Long userId, @Param("content") String content);
    int softDeleteQuestion(@Param("qnaId") Long qnaId);

    int updateQuestionResolved(@Param("qnaId") Long qnaId, @Param("isResolved") String isResolved);

    // 답변
    List<CourseQnaDTO.AnswerRes> selectAnswers(@Param("courseId") Long courseId);

    Long selectAnswerQnaId(@Param("answerId") Long answerId);

    int insertAnswer(@Param("qnaId") Long qnaId,
                     @Param("userId") Long userId,
                     @Param("parentAnswerId") Long parentAnswerId,
                     @Param("content") String content);

    Long selectAnswerOwnerId(@Param("answerId") Long answerId);

    int updateAnswer(@Param("answerId") Long answerId, @Param("content") String content);
    int softDeleteAnswer(@Param("answerId") Long answerId);

}
