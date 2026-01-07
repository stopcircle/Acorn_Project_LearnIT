package com.learnit.learnit.mypage.service;

import com.learnit.learnit.courseVideo.service.CourseVideoService;
import com.learnit.learnit.mypage.dto.QnADTO;
import com.learnit.learnit.mypage.mapper.MyPageQnAMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageQnAService {

    private final MyPageQnAMapper qnAMapper;
    private final CourseVideoService courseVideoService;

    /**
     * 사용자가 작성한 Q&A 목록 조회 (페이징)
     */
    public List<QnADTO> getMyQnAList(Long userId, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 없습니다.");
        }
        int offset = (page - 1) * size;
        List<QnADTO> qnaList = qnAMapper.selectMyQnAList(userId, offset, size);
        
        // 각 Q&A에 대해 첫 번째 챕터 ID 조회 및 설정
        for (QnADTO qna : qnaList) {
            if (qna.getCourseId() != null) {
                Long firstChapterId = courseVideoService.getFirstChapterId(qna.getCourseId().longValue());
                qna.setFirstChapterId(firstChapterId);
            }
        }
        
        return qnaList;
    }

    /**
     * 사용자가 작성한 Q&A 총 개수 조회
     */
    public int getMyQnACount(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 없습니다.");
        }
        return qnAMapper.countMyQnAList(userId);
    }
}

