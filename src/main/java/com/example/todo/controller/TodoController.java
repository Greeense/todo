package com.example.todo.controller;

import com.example.todo.dto.TodoDto;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    
    //새로 할일 등록
    @PostMapping
    public ResponseEntity<?> createTodo(@RequestBody TodoDto dto, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        User user = userRepository.findById(userId).orElseThrow();
        //신규 생성
        Todo todo = Todo.builder()
                .title(dto.getTitle())
                .user(user)
                .build();
        //신규 저장
        todoRepository.save(todo);
        return ResponseEntity.ok("New Todo created");
    }
    //내 할일 조회
    @GetMapping
    public ResponseEntity<?> getTodos(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(todoRepository.findByUser_Id(userId));
    }

    //할일 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchTodos(@RequestParam String keyword, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(todoRepository.findByUser_IdAndTitleContaining(userId, keyword));
    }
}

