package com.learnit.learnit.qna.controller;

import com.learnit.learnit.qna.dto.CourseQnaDTO;
import com.learnit.learnit.qna.service.CourseQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaApiController {

    private final CourseQnaService courseQnaService;

    // ✅✅ 추가: Q&A 목록 조회 (이게 없어서 405 발생)
    @GetMapping("/questions")
    public List<CourseQnaDTO.QuestionRes> list(@RequestParam Long courseId) {
        return courseQnaService.getQuestions(courseId);
    }

    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(@RequestBody CourseQnaDTO.QuestionCreateReq req) {
        try {
            courseQnaService.createQuestion(req.getCourseId(), req.getContent());
            return ResponseEntity.ok().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        }
    }

    @PutMapping("/questions/{qnaId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long qnaId,
                                            @RequestBody CourseQnaDTO.QuestionUpdateReq req) {
        try {
            courseQnaService.updateQuestion(qnaId, req.getContent());
            return ResponseEntity.ok().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        }
    }

    @DeleteMapping("/questions/{qnaId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long qnaId,
                                            @RequestParam Long courseId) {
        try {
            courseQnaService.deleteQuestion(qnaId, courseId);
            return ResponseEntity.ok().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        }
    }

    @PostMapping("/answers")
    public ResponseEntity<?> createAnswer(@RequestBody CourseQnaDTO.AnswerCreateReq req,
                                          @RequestParam Long courseId) {
        try {
            courseQnaService.createAnswer(req, courseId);
            return ResponseEntity.ok().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        }
    }

    @PutMapping("/answers/{answerId}")
    public ResponseEntity<?> updateAnswer(@PathVariable Long answerId,
                                          @RequestBody CourseQnaDTO.AnswerUpdateReq req,
                                          @RequestParam Long courseId) {
        try {
            courseQnaService.updateAnswer(answerId, req, courseId);
            return ResponseEntity.ok().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        }
    }

    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<?> deleteAnswer(@PathVariable Long answerId,
                                          @RequestParam Long courseId) {
        try {
            courseQnaService.deleteAnswer(answerId, courseId);
            return ResponseEntity.ok().build();
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        }
    }
}
