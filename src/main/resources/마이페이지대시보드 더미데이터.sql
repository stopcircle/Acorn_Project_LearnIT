-- ==========================================
-- 대시보드 더미 데이터 생성 스크립트
-- 실제 테이블 구조에 맞게 작성됨
-- ==========================================

-- 1. 카테고리 데이터
INSERT INTO category (category_id, name) VALUES
(1, '프론트엔드'),
(2, '백엔드'),
(3, '데이터베이스'),
(4, '프로젝트 관리'),
(5, '기타');

-- 2. 사용자 데이터
INSERT INTO users (user_id, email, password, name, nickname, phone, region, role, status, provider, profile_img, email_verified, created_at) VALUES
(1, 'test@example.com', '$2a$10$dummy', '홍길동', '길동이', '010-1234-5678', '서울', 'USER', 'ACTIVE', 'local', '/images/profile-default.png', 'Y', NOW()),
(2, 'instructor@example.com', '$2a$10$dummy', '김교수', '김교수', '010-2345-6789', '부산', 'INSTRUCTOR', 'ACTIVE', 'local', '/images/instructor.png', 'Y', NOW()),
(3, 'admin@example.com', '$2a$10$dummy', '관리자', '관리자', '010-3456-7890', '서울', 'ADMIN', 'ACTIVE', 'local', '/images/admin.png', 'Y', NOW());

-- 3. 강의 데이터
INSERT INTO course (course_id, category_id, user_id, title, description, price, thumbnail_url, detail_img_url, open_type, status, delete_flg, created_at) VALUES
(1, 4, 2, '시작하는 PM들을 위한 필수지식', '프로젝트 관리자로 성장하기 위한 필수 지식과 실무 노하우를 배웁니다.', 99000, '/images/course-thumbnail-pm.jpg', '/images/course-detail-pm.jpg', 'ALWAYS', 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, 1, 2, '초보자를 위한 파워포인트', '파워포인트 기초부터 고급 기능까지 단계별로 학습합니다.', 49000, '/images/course-thumbnail-ppt.jpg', '/images/course-detail-ppt.jpg', 'ALWAYS', 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3, 2, 2, '스프링부트 입문', '스프링부트를 활용한 웹 애플리케이션 개발 기초를 학습합니다.', 129000, '/images/course-thumbnail-spring.jpg', '/images/course-detail-spring.jpg', 'ALWAYS', 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(4, 1, 2, 'React 완전정복', 'React를 활용한 프론트엔드 개발의 모든 것을 배웁니다.', 149000, '/images/course-thumbnail-react.jpg', '/images/course-detail-react.jpg', 'ALWAYS', 'ACTIVE', 0, DATE_SUB(NOW(), INTERVAL 15 DAY));

-- 4. 챕터 데이터 (PM 강의 - 29개 챕터)
INSERT INTO chapter (chapter_id, course_id, title, order_index, video_url, duration_sec, created_at) VALUES
-- PM 강의 챕터들
(1, 1, '프로젝트 관리란 무엇인가', 1, 'https://www.youtube.com/watch?v=dummy1', 1800, NOW()),
(2, 1, '프로젝트 기획과 계획 수립', 2, 'https://www.youtube.com/watch?v=dummy2', 2400, NOW()),
(3, 1, '일정 관리와 마일스톤 설정', 3, 'https://www.youtube.com/watch?v=dummy3', 2100, NOW()),
(4, 1, '리소스 관리', 4, 'https://www.youtube.com/watch?v=dummy4', 1950, NOW()),
(5, 1, '리스크 관리', 5, 'https://www.youtube.com/watch?v=dummy5', 2200, NOW()),
(6, 1, '의사소통 관리', 6, 'https://www.youtube.com/watch?v=dummy6', 1800, NOW()),
(7, 1, '품질 관리', 7, 'https://www.youtube.com/watch?v=dummy7', 2000, NOW()),
(8, 1, '변경 관리', 8, 'https://www.youtube.com/watch?v=dummy8', 1900, NOW()),
(9, 1, '팀 빌딩', 9, 'https://www.youtube.com/watch?v=dummy9', 2100, NOW()),
(10, 1, '리더십', 10, 'https://www.youtube.com/watch?v=dummy10', 2300, NOW()),
(11, 1, '의사결정 프로세스', 11, 'https://www.youtube.com/watch?v=dummy11', 2000, NOW()),
(12, 1, '협상 기술', 12, 'https://www.youtube.com/watch?v=dummy12', 2200, NOW()),
(13, 1, '갈등 관리', 13, 'https://www.youtube.com/watch?v=dummy13', 1900, NOW()),
(14, 1, '성과 측정', 14, 'https://www.youtube.com/watch?v=dummy14', 2100, NOW()),
(15, 1, '보고서 작성', 15, 'https://www.youtube.com/watch?v=dummy15', 1800, NOW()),
(16, 1, '프로젝트 도구 활용', 16, 'https://www.youtube.com/watch?v=dummy16', 2400, NOW()),
(17, 1, '애자일 방법론', 17, 'https://www.youtube.com/watch?v=dummy17', 2500, NOW()),
(18, 1, '스크럼 프로세스', 18, 'https://www.youtube.com/watch?v=dummy18', 2200, NOW()),
(19, 1, '칸반 보드 활용', 19, 'https://www.youtube.com/watch?v=dummy19', 2000, NOW()),
(20, 1, '프로젝트 마무리', 20, 'https://www.youtube.com/watch?v=dummy20', 1900, NOW()),
(21, 1, '사후 평가', 21, 'https://www.youtube.com/watch?v=dummy21', 1800, NOW()),
(22, 1, '레슨 런드', 22, 'https://www.youtube.com/watch?v=dummy22', 2000, NOW()),
(23, 1, '고객 만족도 관리', 23, 'https://www.youtube.com/watch?v=dummy23', 2100, NOW()),
(24, 1, '예산 관리', 24, 'https://www.youtube.com/watch?v=dummy24', 2300, NOW()),
(25, 1, '비용 절감 전략', 25, 'https://www.youtube.com/watch?v=dummy25', 2000, NOW()),
(26, 1, '시간 관리', 26, 'https://www.youtube.com/watch?v=dummy26', 1900, NOW()),
(27, 1, '우선순위 설정', 27, 'https://www.youtube.com/watch?v=dummy27', 1800, NOW()),
(28, 1, '위기 대응', 28, 'https://www.youtube.com/watch?v=dummy28', 2200, NOW()),
(29, 1, '프로젝트 성공 사례', 29, 'https://www.youtube.com/watch?v=dummy29', 2400, NOW());

-- 파워포인트 강의 챕터들 (10개)
INSERT INTO chapter (chapter_id, course_id, title, order_index, video_url, duration_sec, created_at) VALUES
(30, 2, '파워포인트 시작하기', 1, 'https://www.youtube.com/watch?v=ppt1', 1200, NOW()),
(31, 2, '슬라이드 기본 조작', 2, 'https://www.youtube.com/watch?v=ppt2', 1500, NOW()),
(32, 2, '텍스트 편집', 3, 'https://www.youtube.com/watch?v=ppt3', 1800, NOW()),
(33, 2, '이미지 삽입과 편집', 4, 'https://www.youtube.com/watch?v=ppt4', 2000, NOW()),
(34, 2, '도형과 SmartArt', 5, 'https://www.youtube.com/watch?v=ppt5', 2200, NOW()),
(35, 2, '차트 만들기', 6, 'https://www.youtube.com/watch?v=ppt6', 2400, NOW()),
(36, 2, '애니메이션 적용', 7, 'https://www.youtube.com/watch?v=ppt7', 2100, NOW()),
(37, 2, '슬라이드 쇼 설정', 8, 'https://www.youtube.com/watch?v=ppt8', 1900, NOW()),
(39, 2, '템플릿 활용', 9, 'https://www.youtube.com/watch?v=ppt9', 1800, NOW()),
(40, 2, '프레젠테이션 실전', 10, 'https://www.youtube.com/watch?v=ppt10', 2500, NOW());

-- 5. 수강 등록 데이터
INSERT INTO enrollment (enrollment_id, user_id, course_id, status, progress_rate, created_at) VALUES
(1, 1, 1, 'ACTIVE', 10, DATE_SUB(NOW(), INTERVAL 15 DAY)),  -- PM 강의 수강 중 (10%)
(2, 1, 2, 'ACTIVE', 100, DATE_SUB(NOW(), INTERVAL 90 DAY)), -- 파워포인트 강의 완료
(3, 1, 3, 'ACTIVE', 5, DATE_SUB(NOW(), INTERVAL 5 DAY));    -- 스프링부트 강의 수강 중 (5%)

-- 6. 학습 로그 데이터 (study_log) - 최근 학습 기록
INSERT INTO study_log (log_id, user_id, course_id, chapter_id, studied_sec, created_at) VALUES
-- PM 강의 학습 기록 (3개 챕터 완료)
(1, 1, 1, 1, 1800, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 1, 1, 1, 300, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, 1, 1, 2, 2400, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4, 1, 1, 3, 2100, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 1, 1, 3, 200, DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- 주간 학습 기록 (화요일)
(6, 1, 1, 4, 1950, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 1, 3, 1, 1200, DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- 파워포인트 강의 완료 기록
(8, 1, 2, 30, 1200, DATE_SUB(NOW(), INTERVAL 85 DAY)),
(9, 1, 2, 31, 1500, DATE_SUB(NOW(), INTERVAL 83 DAY)),
(10, 1, 2, 32, 1800, DATE_SUB(NOW(), INTERVAL 80 DAY)),
(11, 1, 2, 33, 2000, DATE_SUB(NOW(), INTERVAL 78 DAY)),
(12, 1, 2, 34, 2200, DATE_SUB(NOW(), INTERVAL 75 DAY)),
(13, 1, 2, 35, 2400, DATE_SUB(NOW(), INTERVAL 72 DAY)),
(14, 1, 2, 36, 2100, DATE_SUB(NOW(), INTERVAL 70 DAY)),
(15, 1, 2, 37, 1900, DATE_SUB(NOW(), INTERVAL 68 DAY)),
(16, 1, 2, 39, 1800, DATE_SUB(NOW(), INTERVAL 65 DAY)),
(17, 1, 2, 40, 2500, DATE_SUB(NOW(), INTERVAL 60 DAY));

-- 7. 수료증 데이터
INSERT INTO certificate (cert_id, enrollment_id, certificate_number, is_approved, issued_at) VALUES
(1, 2, 'CERT-2024-0001', 'Y', '2024-07-30 10:00:00'),  -- 파워포인트 강의 수료증
(2, 1, 'CERT-2024-0002', 'N', NULL);  -- PM 강의는 아직 미수료

-- 8. 쿠폰 데이터
INSERT INTO coupon (coupon_id, name, type, discount_amount, min_price, expire_date, created_at) VALUES
(1, '신규 회원 환영 쿠폰', 'AUTO', 10000, 50000, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
(2, '여름 특가 쿠폰', 'MANUAL', 20000, 100000, DATE_ADD(NOW(), INTERVAL 60 DAY), NOW()),
(3, '무료 강의 쿠폰', 'AUTO', 50000, 0, DATE_ADD(NOW(), INTERVAL 90 DAY), NOW());

-- 9. 사용자 쿠폰 데이터
INSERT INTO user_coupon (user_coupon_id, user_id, coupon_id, is_used, issued_at) VALUES
(1, 1, 1, 'N', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 1, 3, 'Y', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, 1, 2, 'N', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 10. 결제 데이터
INSERT INTO payment (payment_id, user_id, total_price, final_price, method, coupon_id, created_at) VALUES
(1, 1, 99000, 99000, 'CARD', NULL, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 1, 49000, 0, 'COUPON', 3, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(3, 1, 129000, 109000, 'CARD', 1, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 11. 결제 상세 데이터
INSERT INTO payment_detail (pay_detail_id, payment_id, course_id, price) VALUES
(1, 1, 1, 99000),
(2, 2, 2, 49000),
(3, 3, 3, 129000);

-- 12. Q&A 질문 데이터
INSERT INTO qna_question (qna_id, course_id, user_id, title, content, created_at, is_resolved) VALUES
(1, 1, 1, '프로젝트 일정이 지연될 때 어떻게 해야 하나요?', '프로젝트 일정이 예상보다 지연되고 있습니다. 어떻게 대응해야 할까요?', DATE_SUB(NOW(), INTERVAL 3 DAY), 'N'),
(2, 1, 1, '팀원 간 갈등이 발생했습니다', '프로젝트 진행 중 팀원 간 의견 충돌이 발생했습니다. 어떻게 해결해야 할까요?', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Y');

-- 13. Q&A 답변 데이터
INSERT INTO qna_answer (answer_id, qna_id, user_id, content, created_at) VALUES
(1, 2, 2, '갈등 해결을 위해서는 먼저 양쪽의 의견을 충분히 듣고, 공통된 목표를 상기시키는 것이 중요합니다.', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 14. 리뷰 데이터
INSERT INTO review (review_id, course_id, user_id, rating, content, created_at, delete_flg, comment_status) VALUES
(1, 2, 1, 5, '파워포인트 기초부터 고급까지 체계적으로 배울 수 있어서 좋았습니다!', DATE_SUB(NOW(), INTERVAL 60 DAY), 0, 'ACTIVE'),
(2, 1, 1, 4, 'PM 실무에 도움이 되는 내용이 많아서 유용했습니다.', DATE_SUB(NOW(), INTERVAL 10 DAY), 0, 'ACTIVE');

-- 15. 공지사항 데이터
INSERT INTO notice (notice_id, user_id, category, title, content, created_at) VALUES
(1, 3, '일반', '시스템 점검 안내', '2024년 12월 20일 새벽 2시부터 4시까지 시스템 점검이 진행됩니다.', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 3, '이벤트', '연말 특별 할인 이벤트', '연말을 맞아 모든 강의 20% 할인 이벤트를 진행합니다.', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 3, '긴급', '서버 장애 복구 완료', '일시적인 서버 장애가 발생했으나 현재 정상화되었습니다.', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 16. 퀴즈 데이터
INSERT INTO quiz (quiz_id, course_id, chapter_id, type, target_user_id) VALUES
(1, 1, 1, 'CHAPTER', NULL),
(2, 1, 2, 'CHAPTER', NULL),
(3, 2, 30, 'CHAPTER', NULL);

-- 17. 문제 데이터
INSERT INTO question (question_id, quiz_id, content, q_type, explanation) VALUES
(1, 1, '프로젝트 관리의 핵심 요소는 무엇인가요?', 'MULTIPLE_CHOICE', '프로젝트 관리는 일정, 예산, 품질, 리스크 관리가 핵심입니다.'),
(2, 1, '프로젝트 일정 지연 시 가장 먼저 해야 할 일은?', 'MULTIPLE_CHOICE', '일정 지연 시 원인 분석이 가장 중요합니다.'),
(3, 2, '마일스톤의 역할은?', 'MULTIPLE_CHOICE', '마일스톤은 프로젝트의 주요 단계를 표시하는 중요한 지점입니다.');

-- 18. 퀴즈 옵션 데이터
INSERT INTO quiz_option (option_id, question_id, content, is_correct) VALUES
(1, 1, '일정 관리', 'T'),
(2, 1, '예산 관리', 'F'),
(3, 1, '품질 관리', 'F'),
(4, 1, '모두 해당', 'F'),
(5, 2, '원인 분석', 'T'),
(6, 2, '팀원 교체', 'F'),
(7, 2, '일정 연장', 'F'),
(8, 3, '프로젝트 단계 표시', 'T'),
(9, 3, '예산 할당', 'F'),
(10, 3, '팀 구성', 'F');

-- 19. 사용자 답변 데이터
INSERT INTO user_answer (answer_id, user_id, question_id, sel_option_id, is_correct, answered_at) VALUES
(1, 1, 1, 1, 'Y', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 1, 2, 5, 'Y', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, 1, 3, 8, 'Y', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 20. 오답 노트 데이터
INSERT INTO wrong_note (note_id, user_id, question_id, quiz_id, created_at) VALUES
(1, 1, 2, 1, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 1, 3, 2, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 21. 장바구니 데이터
INSERT INTO cart (cart_id, user_id, course_id) VALUES
(1, 1, 4);  -- React 강의를 장바구니에 추가
