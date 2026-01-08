package com.learnit.learnit.admin;

import com.learnit.learnit.admin.home.AdminHome;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMapper {
    int countActiveCourses();
    int countPendingQnas();
    int countPendingReviews();

    List<AdminHome.AdminNotice> selectLatestNotices();
    List<AdminHome.AdminCertificate> selectPendingCertificates();
}
