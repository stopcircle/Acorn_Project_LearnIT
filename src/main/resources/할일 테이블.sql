-- ==========================================
-- 할일(Todo) 테이블 생성 스크립트
-- ==========================================

CREATE TABLE IF NOT EXISTS todo (
    todo_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    target_date DATE NOT NULL,
    is_completed CHAR(1) DEFAULT 'N',
    completed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, target_date),
    INDEX idx_target_date (target_date)
);

