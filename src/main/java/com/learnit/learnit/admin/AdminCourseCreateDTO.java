package com.learnit.learnit.admin;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class AdminCourseCreateDTO {
    private Long courseId; // 생성된 ID 반환용
    private String title;
    private String description;
    private Long categoryId;
    private int price;
    private List<Long> instructorIds; // UI에서 여러 명 선택 가능
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private boolean alwaysOpen;

    // 파일 업로드 (입력)
    private MultipartFile thumbnail;
    private MultipartFile detailThumbnail;

    // 파일 경로 (DB 저장용)
    private String thumbnailUrl;
    private String detailImgUrl;

    // 커리큘럼 (섹션 + 챕터)
    private List<SectionRequest> sections;

    @Data
    public static class SectionRequest {
        private String title;
        private List<ChapterRequest> chapters;
    }

    @Data
    public static class ChapterRequest {
        private String title;
        private String videoUrl;
        private MultipartFile file; // 챕터 자료 파일
    }
}
