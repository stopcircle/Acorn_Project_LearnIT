package com.learnit.learnit.courseVideo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnit.learnit.courseVideo.dto.CourseFile;
import com.learnit.learnit.courseVideo.dto.CourseVideo;
import com.learnit.learnit.courseVideo.dto.CurriculumSection;
import com.learnit.learnit.courseVideo.repository.CourseVideoMapper;
import com.learnit.learnit.mypage.mapper.MyCertificateMapper;
import com.learnit.learnit.quiz.dto.Quiz;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseVideoService {
    private final CourseVideoMapper courseVideoMapper;
    private final MyCertificateMapper certificateMapper;

    @Value("${rapid-api.key}")
    private String rapidApiKey;

    @Value("${rapid-api.host}")
    private String rapidApiHost;

    @Value("${rapid-api.url}")
    private String judge0Url;

    public CourseVideo getChapterDetail(Long chapterId){
        return courseVideoMapper.findById(chapterId);
    }

    public Long getPrevChapterId(Long courseId, Long currentOrder) {
        return courseVideoMapper.selectPrevChapterId(courseId, currentOrder);
    }

    public Long getNextChapterId(Long courseId, Long currentOrder) {
        return courseVideoMapper.selectNextChapterId(courseId, currentOrder);
    }

    @Transactional
    public void saveStudyLog(Long userId, Long courseId, Long chapterId, Integer playTime) {
        // 학습 로그 저장
        courseVideoMapper.insertOrUpdateStudyLog(userId, courseId, chapterId, playTime);
        
        // 완강 여부 체크 및 수료증 자동 발급
        checkAndIssueCertificate(userId, courseId);
    }
    
    /**
     * 완강 여부 체크 및 수료증 자동 발급
     */
    private void checkAndIssueCertificate(Long userId, Long courseId) {
        try {
            // 전체 챕터 수 확인
            int totalChapters = courseVideoMapper.countTotalChapters(courseId);
            if (totalChapters == 0) {
                return; // 챕터가 없으면 수료증 발급 불가
            }
            
            // 완료한 챕터 수 확인 (95% 이상 수강한 챕터)
            int completedChapters = courseVideoMapper.countCompletedChapters(userId, courseId);
            
            // 모든 챕터를 완료했는지 확인
            if (completedChapters >= totalChapters) {
                // enrollment_id 조회
                Long enrollmentId = courseVideoMapper.selectEnrollmentId(userId, courseId);
                if (enrollmentId == null) {
                    log.warn("Enrollment not found: userId={}, courseId={}", userId, courseId);
                    return;
                }
                
                // 이미 수료증이 발급되었는지 확인
                if (courseVideoMapper.existsCertificate(enrollmentId)) {
                    log.debug("Certificate already exists: enrollmentId={}", enrollmentId);
                    return;
                }
                
                // enrollment 완강 처리
                courseVideoMapper.updateEnrollmentCompleted(enrollmentId);
                
                // 수료증 자동 생성
                createCertificate(enrollmentId, userId, courseId);
                
                log.info("Certificate auto-issued: userId={}, courseId={}, enrollmentId={}", 
                    userId, courseId, enrollmentId);
            }
        } catch (Exception e) {
            log.error("Error checking completion and issuing certificate: userId={}, courseId={}", 
                userId, courseId, e);
            // 수료증 발급 실패해도 학습 로그 저장은 성공했으므로 예외를 던지지 않음
        }
    }
    
    /**
     * 수료증 생성
     */
    private void createCertificate(Long enrollmentId, Long userId, Long courseId) {
        // 수료증 번호 생성 (CERT-YYYYMMDD-ENROLLMENT_ID 형식)
        String certificateNumber = "CERT-" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
            "-" + enrollmentId;
        
        certificateMapper.insertCertificate(enrollmentId, certificateNumber);
        log.info("Certificate created: enrollmentId={}, certificateNumber={}", 
            enrollmentId, certificateNumber);
    }

    public int getProgressPercent(Long userId, Long courseId) {
        int total = courseVideoMapper.countTotalChapters(courseId);
        if (total == 0) return 0;
        int completed = courseVideoMapper.countCompletedChapters(userId, courseId);

        return (int) ((double) completed / total * 100);
    }

    public void updateChapterDuration(Long chapterId, int duration) {
        courseVideoMapper.updateChapterDuration(chapterId, duration);
    }

    public List<CurriculumSection> getCurriculumGrouped(Long courseId) {
        List<CourseVideo> allChapters = courseVideoMapper.selectChapterList(courseId);

        Map<String, List<CourseVideo>> grouped = allChapters.stream()
                .collect(Collectors.groupingBy(
                        ch -> ch.getSectionTitle() == null ? "기타" : ch.getSectionTitle(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<CurriculumSection> result = new ArrayList<>();
        for (Map.Entry<String, List<CourseVideo>> entry : grouped.entrySet()) {
            result.add(new CurriculumSection(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    public List<CourseFile> getCourseResources(Long courseId) {
        return courseVideoMapper.selectCourseResources(courseId);
    }

    public Long getNextQuizId(CourseVideo currentChapter, Long nextChapterId, Map<String, Quiz> quizMap) {

        if (!quizMap.containsKey(currentChapter.getSectionTitle())) {
            return null;
        }
        Quiz sectionQuiz = quizMap.get(currentChapter.getSectionTitle());

        if (nextChapterId == null) {
            return sectionQuiz.getQuizId();
        }

        CourseVideo nextChapter = this.getChapterDetail(nextChapterId);

        if (nextChapter != null && !nextChapter.getSectionTitle().equals(currentChapter.getSectionTitle())) {
            return sectionQuiz.getQuizId();
        }

        return null;
    }

    public boolean isUserEnrolled(Long userId, Long courseId) {
        String status = courseVideoMapper.selectEnrollmentStatus(userId, courseId);

        return "ACTIVE".equals(status);
    }

    public boolean isInstructor(Long userId, Long courseId) {
        Long instructorId = courseVideoMapper.selectCourseInstructorId(courseId);
        return userId != null && userId.equals(instructorId);
    }

    public void saveInterpreterLog(Long userId, Long courseId, Long chapterId, Integer languageId) {
        courseVideoMapper.insertInterpreterLog(userId, courseId, chapterId, languageId);
    }

    public Long getFirstChapterId(Long courseId) {
        return courseVideoMapper.selectFirstChapterId(courseId);
    }

    public Map<String, Object> runInterpreterCode(String sourceCode, String languageId) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 변수(rapidApiKey, rapidApiHost)를 사용
        headers.set("X-RapidAPI-Key", rapidApiKey);
        headers.set("X-RapidAPI-Host", rapidApiHost);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("source_code", sourceCode);
        requestBody.put("language_id", Integer.parseInt(languageId));

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(requestBody);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            // 변수(judge0Url) 사용
            ResponseEntity<Map> response = restTemplate.postForEntity(judge0Url, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> result = new HashMap<>();

            if (responseBody != null) {
                String stdout = (String) responseBody.get("stdout");
                String stderr = (String) responseBody.get("stderr");
                String compileOutput = (String) responseBody.get("compile_output");

                if (stdout != null) result.put("output", stdout);
                else if (stderr != null) result.put("output", "에러 발생:\n" + stderr);
                else if (compileOutput != null) result.put("output", "컴파일 에러:\n" + compileOutput);
                else result.put("output", "실행 완료 (출력값 없음)");
            } else {
                result.put("output", "결과가 없습니다.");
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("output", "서버 오류: " + e.getMessage());
            return error;
        }
    }
}
