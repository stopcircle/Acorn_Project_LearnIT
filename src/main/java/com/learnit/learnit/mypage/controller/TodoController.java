package com.learnit.learnit.mypage.controller;

import com.learnit.learnit.mypage.dto.TodoDTO;
import com.learnit.learnit.mypage.service.TodoService;
import com.learnit.learnit.mypage.service.UserIdentityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mypage/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final UserIdentityService userIdentityService;

    /**
     * 날짜별 할일 목록 조회 (AJAX용)
     */
    @GetMapping("/list")
    @ResponseBody
    public List<TodoDTO> getTodos(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            HttpSession session) {
        // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
        Long userId = userIdentityService.requireCurrentUserId(session);
        return todoService.getTodosByDate(userId, year, month, day);
    }

    /**
     * 할일 저장 (AJAX용)
     */
    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> saveTodo(@RequestBody TodoDTO todo, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
            Long userId = userIdentityService.requireCurrentUserId(session);
            todo.setUserId(userId);
            
            // description이 null인 경우 빈 문자열로 설정
            if (todo.getDescription() == null) {
                todo.setDescription("");
            }
            
            TodoDTO savedTodo = todoService.saveTodo(todo);
            
            if (savedTodo == null || savedTodo.getTodoId() == null) {
                response.put("success", false);
                response.put("error", "할일 저장 후 데이터를 가져올 수 없습니다.");
                return response;
            }
            
            response.put("success", true);
            response.put("todo", savedTodo);
            return response;
        } catch (Exception e) {
            System.err.println("할일 저장 컨트롤러 오류: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    /**
     * 할일 완료 처리 (AJAX용)
     */
    @PutMapping("/{todoId}/complete")
    @ResponseBody
    public Map<String, Object> completeTodo(
            @PathVariable Long todoId,
            @RequestParam boolean completed,
            HttpSession session) {
        // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
        Long userId = userIdentityService.requireCurrentUserId(session);
        
        TodoDTO todo = todoService.completeTodo(todoId, userId, completed);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("todo", todo);
        return response;
    }

    /**
     * 할일 삭제 (AJAX용)
     */
    @DeleteMapping("/{todoId}")
    @ResponseBody
    public Map<String, Object> deleteTodo(@PathVariable Long todoId, HttpSession session) {
        // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
        Long userId = userIdentityService.requireCurrentUserId(session);
        
        todoService.deleteTodo(todoId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    /**
     * 할일 일괄 저장 (날짜별) (AJAX용)
     */
    @PostMapping("/batch")
    @ResponseBody
    public Map<String, Object> saveTodosBatch(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day,
            @RequestBody List<TodoDTO> todos,
            HttpSession session) {
        // 사용자 식별 서비스를 통해 현재 사용자 ID 조회
        Long userId = userIdentityService.requireCurrentUserId(session);
        
        LocalDate targetDate = LocalDate.of(year, month, day);
        todoService.saveTodosBatch(userId, targetDate, todos);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}

