package com.learnit.learnit.payment.common.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnrollmentMapper {

    //결제 성공 시 수강 등록
    void insertEnrollment(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );

    //중복 강의 수강 체크
    boolean existsEnrollment(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );
}
