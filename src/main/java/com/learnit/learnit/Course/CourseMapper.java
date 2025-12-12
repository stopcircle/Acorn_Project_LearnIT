package com.learnit.learnit.Course;

import com.learnit.learnit.Course.Course;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CourseMapper {
    // DB에서 리스트 가져오는 메서드 정의
    List<Course> getTestList();
}