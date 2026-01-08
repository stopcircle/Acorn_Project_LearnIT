package com.learnit.learnit.mypage.mapper;

import com.learnit.learnit.mypage.dto.QnADTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyQnAMapper {

    /**
     * 사용자가 작성한 Q&A 목록 조회 (페이징)
     */
    List<QnADTO> selectMyQnAList(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 사용자가 작성한 Q&A 총 개수 조회
     */
    int countMyQnAList(@Param("userId") Long userId);
}

