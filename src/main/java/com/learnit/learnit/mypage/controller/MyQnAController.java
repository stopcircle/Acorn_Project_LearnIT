package com.learnit.learnit.mypage.controller;

import com.learnit.learnit.mypage.dto.QnADTO;
import com.learnit.learnit.mypage.service.MyQnAService;
import com.learnit.learnit.user.dto.UserDTO;
import com.learnit.learnit.user.service.UserService;
import com.learnit.learnit.user.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/mypage/qa")
@RequiredArgsConstructor
public class MyQnAController {

    private final MyQnAService qnAService;
    private final UserService userService;

    private static final int PAGE_BLOCK_SIZE = 5;

    /**
     * 마이페이지 강의 Q&A 목록 조회
     */
    @GetMapping
    public String qnaList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "4") int size,
            Model model, 
            HttpSession session) {
        Long userId = SessionUtils.getUserId(session);
        
        if (userId == null) {
            return "redirect:/login";
        }

        // 사용자 정보 조회
        UserDTO user = userService.getUserDTOById(userId);
        model.addAttribute("user", user);

        // 페이징 처리
        int totalCount = qnAService.getMyQnACount(userId);
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages <= 0) totalPages = 1;

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startPage = ((page - 1) / PAGE_BLOCK_SIZE) * PAGE_BLOCK_SIZE + 1;
        int endPage = Math.min(startPage + PAGE_BLOCK_SIZE - 1, totalPages);

        // 사용자가 작성한 Q&A 목록 조회
        List<QnADTO> qnaList = qnAService.getMyQnAList(userId, page, size);
        model.addAttribute("qnaList", qnaList != null ? qnaList : new java.util.ArrayList<>());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageSize", size);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "mypage/qna/myQna";
    }
}
