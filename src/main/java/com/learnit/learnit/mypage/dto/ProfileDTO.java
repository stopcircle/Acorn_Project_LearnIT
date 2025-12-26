package com.learnit.learnit.mypage.dto;

import lombok.Data;
import java.util.List;

/**
 * 프로필 조회용 DTO
 * 프로필 상세, 배지, GitHub 분석, 스킬 차트, 참여/완료 스터디 정보 포함
 */
@Data
public class ProfileDTO {
    private Long userId;
    private String nickname;
    private String username;
    private String email;
    private String region; // 지역
    private String githubUrl; // 깃허브 주소
    private String profileImageUrl;
    
    // 프로필 상세 정보
    private String name; // 이름
    private String phone; // 전화번호
    
    // 배지 정보 (추후 확장)
    private List<BadgeDTO> badges;
    
    // GitHub 분석 결과 (추후 확장)
    private GitHubAnalysisDTO githubAnalysis;
    
    // 기술 스킬 차트 (추후 확장)
    private SkillChartDTO skillChart;
    
    // 참여/완료 스터디 (추후 확장)
    private List<StudyDTO> participatingStudies;
    private List<StudyDTO> completedStudies;
    
    // 임시용 내부 클래스들 (추후 별도 DTO로 분리 가능)
    @Data
    public static class BadgeDTO {
        private String badgeName;
        private String badgeImageUrl;
    }
    
    @Data
    public static class GitHubAnalysisDTO {
        private String analysisData; // JSON 형태 또는 객체로 확장 가능
    }
    
    @Data
    public static class SkillChartDTO {
        private String chartData; // JSON 형태 또는 객체로 확장 가능
    }
    
    @Data
    public static class StudyDTO {
        private Long studyId;
        private String studyName;
        private String studyStatus;
    }
}

