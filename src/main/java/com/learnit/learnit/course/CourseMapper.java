package com.learnit.learnit.course;

import com.learnit.learnit.home.MainCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {

    List<CourseDTO> searchCourses(@Param("keyword") String keyword);

    List<MainCourse> selectPopularCourse(int limit);
    List<MainCourse> selectBannerCourse(int limit);

    List<CourseDTO> selectCourses(@Param("categoryId") Integer categoryId,
                                  @Param("sort") String sort,
                                  @Param("tab") String tab);

    List<CourseDTO> selectCoursesPage(@Param("categoryId") Integer categoryId,
                                      @Param("sort") String sort,
                                      @Param("tab") String tab,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);

    long countCourses(@Param("categoryId") Integer categoryId,
                      @Param("tab") String tab);

    // ✅ 추가
    CourseDTO selectCourseById(@Param("courseId") int courseId);
}
