package com.learnit.learnit.mapper;

import com.learnit.learnit.course.Course;
import com.learnit.learnit.home.MainCourse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseMapper {
    //
    List<Course> searchCourses(String keyword);

    //메인 페이지
    //인기 강의 목록 불러오기
    List<MainCourse> selectPopularCourse(int limit);

    //썸네일배너 강의 불러오기
    List<MainCourse> selectBannerCourse(int limit);
}
