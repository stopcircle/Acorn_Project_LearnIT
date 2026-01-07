package com.learnit.learnit.admin.qna;

import com.learnit.learnit.user.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class AdminQnaAuth {

    private static final long SUPER_ADMIN_USER_ID = 4L;

    public Long loginUserId(HttpSession session) {
        return SessionUtils.getUserId(session);
    }

    public String loginUserRole(HttpSession session) {
        if (session == null) return null;
        Object r = session.getAttribute("LOGIN_USER_ROLE");
        return r == null ? null : r.toString();
    }

    /** ✅ 전체 권한(전체Q&A 포함): user_id=4 또는 role=ADMIN */
    public boolean isAdmin(HttpSession session) {
        Long id = loginUserId(session);
        if (id != null && id == SUPER_ADMIN_USER_ID) return true;

        String role = loginUserRole(session);
        return "ADMIN".equalsIgnoreCase(role);
    }

    /** ✅ 강의Q&A만 처리: role=SUB_ADMIN */
    public boolean isSubAdmin(HttpSession session) {
        String role = loginUserRole(session);
        return "SUB_ADMIN".equalsIgnoreCase(role);
    }

    /** ✅ 강의 Q&A 처리 가능? (ADMIN/SUB_ADMIN/슈퍼) */
    public boolean canManageLecture(HttpSession session) {
        return isAdmin(session) || isSubAdmin(session);
    }

    /** ✅ 전체(사이트) Q&A 처리 가능? (ADMIN/슈퍼) */
    public boolean canManageSite(HttpSession session) {
        return isAdmin(session);
    }

    /** ✅ 특정 Q&A row를 수정/삭제/상태변경/답변저장 할 수 있나? */
    public boolean canManageRow(HttpSession session, Integer courseId) {
        // courseId != null => 강의 Q&A
        if (courseId != null) return canManageLecture(session);
        // courseId == null => 전체(사이트) Q&A
        return canManageSite(session);
    }
}
