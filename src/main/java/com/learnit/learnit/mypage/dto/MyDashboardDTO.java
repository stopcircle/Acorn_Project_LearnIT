package com.learnit.learnit.mypage.dto;

import lombok.Data;

@Data
public class MyDashboardDTO {
    private MyCourseSummaryDTO recentCourse;
    private MyWeeklyLearningDTO weeklyLearning;
    private MyCertificateDTO latestCertificate;
    private MyCalendarSummaryDTO calendarSummary;
}

