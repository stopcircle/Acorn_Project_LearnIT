package com.learnit.learnit.admin;

import com.learnit.learnit.category.CategoryService;
import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private final AdminCourseMapper adminCourseMapper;
    private final UserMapper userMapper;

    @Value("${file.upload-dir.course:uploads/course}")
    private String courseUploadDir;

    @Transactional
    public void createCourse(AdminCourseCreateDTO dto) {
        if (dto.isAlwaysOpen()) {
            dto.setStartDate(null);
            dto.setEndDate(null);
        }

        try {
            saveThumbnails(dto);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }

        adminCourseMapper.insertCourse(dto);
        
        saveCurriculum(dto);
    }

    private void saveThumbnails(AdminCourseCreateDTO dto) throws IOException {
        // 썸네일 저장
        if (dto.getThumbnail() != null && !dto.getThumbnail().isEmpty()) {
            String path = saveFile(dto.getThumbnail());
            dto.setThumbnailUrl(path);
        }

        // 상세 이미지(또는 PDF) 저장
        if (dto.getDetailThumbnail() != null && !dto.getDetailThumbnail().isEmpty()) {
            MultipartFile file = dto.getDetailThumbnail();
            String originalFilename = file.getOriginalFilename();
            
            if (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf")) {
                String textPath = savePdfAsText(file);
                dto.setDetailImgUrl(textPath);
            } else {
                String path = saveFile(file);
                dto.setDetailImgUrl(path);
            }
        }
    }

    private void saveCurriculum(AdminCourseCreateDTO dto) {
        if (dto.getSections() == null) return;

        Long courseId = dto.getCourseId();
        int totalOrderIndex = 1;

        for (AdminCourseCreateDTO.SectionRequest section : dto.getSections()) {
            if (section == null || section.getChapters() == null) continue;

            for (AdminCourseCreateDTO.ChapterRequest chapterReq : section.getChapters()) {
                if (chapterReq == null) continue;

                // 1. 챕터 DB 저장
                AdminChapterInsertDTO chapterDto = new AdminChapterInsertDTO();
                chapterDto.setCourseId(courseId);
                chapterDto.setSectionTitle(section.getTitle());
                chapterDto.setTitle(chapterReq.getTitle());
                chapterDto.setOrderIndex(totalOrderIndex++);
                chapterDto.setVideoUrl(chapterReq.getVideoUrl());

                adminCourseMapper.insertChapter(chapterDto);
                Long chapterId = chapterDto.getChapterId();

                // 2. 챕터 자료 파일 업로드 및 저장
                saveChapterResource(chapterId, chapterReq);
            }
        }
    }

    private void saveChapterResource(Long chapterId, AdminCourseCreateDTO.ChapterRequest chapterReq) {
        if (chapterReq.getFile() != null && !chapterReq.getFile().isEmpty()) {
            try {
                String fileUrl = saveFile(chapterReq.getFile());
                String originalFilename = chapterReq.getFile().getOriginalFilename();
                String fileType = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                }

                adminCourseMapper.insertChapterResource(chapterId, originalFilename, fileUrl, fileType);
            } catch (IOException e) {
                throw new RuntimeException("챕터 자료 파일 저장 중 오류가 발생했습니다.", e);
            }
        }
    }

    private Path getUploadPath() throws IOException {
        String uploadDir = (courseUploadDir != null && !courseUploadDir.isEmpty()) 
                ? courseUploadDir 
                : "uploads/course";
        
        if (!Paths.get(uploadDir).isAbsolute()) {
            uploadDir = System.getProperty("user.dir") + File.separator + uploadDir;
        }
                
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    private String savePdfAsText(MultipartFile file) throws IOException {
        File tempPdf = File.createTempFile("temp", ".pdf");
        file.transferTo(tempPdf);

        String text;
        try (PDDocument document = Loader.loadPDF(tempPdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            text = stripper.getText(document);
        } finally {
            tempPdf.delete();
        }

        String savedFilename = UUID.randomUUID().toString() + ".txt";
        Path filePath = getUploadPath().resolve(savedFilename);
        Files.writeString(filePath, text, StandardCharsets.UTF_8);

        return "/uploads/course/" + savedFilename;
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String savedFilename = UUID.randomUUID().toString() + extension;
        Path filePath = getUploadPath().resolve(savedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/course/" + savedFilename;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> searchInstructors(String keyword) {
        return userMapper.searchInstructors(keyword);
    }

    @Transactional(readOnly = true)
    public List<AdminCourse> getCourses(int page, int size, String status, String search) {
        int offset = (page - 1) * size;
        return adminCourseMapper.selectCourses(offset, size, status, search);
    }

    @Transactional(readOnly = true)
    public int getCourseCount(String status, String search) {
        return adminCourseMapper.countCourses(status, search);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        adminCourseMapper.deleteCourse(courseId);
    }
}
