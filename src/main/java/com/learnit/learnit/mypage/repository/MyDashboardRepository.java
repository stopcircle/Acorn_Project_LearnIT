package com.learnit.learnit.mypage.repository;

import com.learnit.learnit.mypage.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MyDashboardRepository {
    // 최근 학습 강의 조회
    MyCourseSummaryDTO selectRecentCourse(@Param("userId") Long userId);

    // 주간 학습 데이터 조회
    List<MyWeeklyLearningDTO.DailyLearning> selectWeeklyLearning(
            @Param("userId") Long userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 최근 수료증 조회
    MyCertificateDTO selectLatestCertificate(@Param("userId") Long userId);

    // 캘린더 데이터 조회 (월별)
    List<MyCalendarSummaryDTO.CalendarDay> selectCalendarData(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    // 특정 날짜의 수강한 강의 목록 조회
    List<MyDailyCourseDTO> selectDailyCourses(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("day") Integer day
    );

    // 날짜별 할일 조회
    List<MyTodoDTO> selectTodosByDate(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("day") int day
    );
    
    // 할일 저장
    int insertTodo(MyTodoDTO todo);
    
    // 할일 수정
    int updateTodo(MyTodoDTO todo);
    
    // 할일 삭제
    int deleteTodo(@Param("todoId") Long todoId, @Param("userId") Long userId);
    
    // 할일 ID로 조회
    MyTodoDTO selectTodoById(@Param("todoId") Long todoId, @Param("userId") Long userId);
    
    // 월별 할일 목록 조회 (미완료만)
    List<MyTodoDTO> selectTodosByMonth(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );

    // 일일 학습 목표 저장 또는 업데이트
    void upsertDailyGoal(MyDailyGoalDTO goal);

    // 현재 주의 목표 조회
    MyDailyGoalDTO selectCurrentDailyGoal(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
