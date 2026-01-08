package com.learnit.learnit.mypage.mapper;

import com.learnit.learnit.mypage.dto.MyCertificateDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MyCertificateMapper {
    /**
     * 수료증 상세 조회
     */
    MyCertificateDTO selectCertificateById(@Param("certificateId") Long certificateId, @Param("userId") Long userId);
}
