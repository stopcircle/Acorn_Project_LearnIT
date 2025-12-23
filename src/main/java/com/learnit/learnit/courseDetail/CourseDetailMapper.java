package com.learnit.learnit.courseDetail;

import com.learnit.learnit.course.CourseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseDetailMapper {
    CourseDTO selectCourseDetail(@Param("courseId") int courseId);
}
