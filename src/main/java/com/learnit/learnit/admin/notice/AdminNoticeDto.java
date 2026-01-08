package com.learnit.learnit.admin.notice;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminNoticeDTO {
    private Integer noticeId;
    private Long userId;
    private String category;
    private String title;
    private String content;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String writerName;
    
    // 파일명 추출용 (enrichOriginalFileName에서 설정)
    private String originalFileName;
}
