package com.learnit.learnit.mypage.mapper;

import com.learnit.learnit.mypage.dto.MyCourseSummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyCoursesMapper {
    List<MyCourseSummaryDTO> selectMyCourses(@Param("userId") Long userId);
}
