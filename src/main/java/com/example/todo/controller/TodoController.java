package com.example.todo.controller;

import com.example.todo.dto.ResponseDto;
import com.example.todo.dto.TodoDto;
import com.example.todo.service.TodoService;
import com.example.todo.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    //새 todo 생성
    @PostMapping
    public ResponseEntity<?> createTodo(@RequestBody @Valid TodoDto.create todoDto, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        LogUtil.debug(this.getClass(), "[TodoController/deleteTodo] userId={}",userId);

        todoService.createTodo(userId, todoDto);
        LogUtil.info(this.getClass(),"[TodoController/createTodo] create todo success");

        return ResponseEntity.ok(new ResponseDto("Todo create Success"));
    }

    //내 todo 리스트 조회
    @GetMapping
    public ResponseEntity<?> getTodos(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        LogUtil.debug(this.getClass(), "[TodoController/deleteTodo] userId={}",userId);

        List<TodoDto.general> todos = todoService.getTodos(userId);
        LogUtil.info(this.getClass(),"[TodoController/getTodos] get todos success");

        return ResponseEntity.ok(todos);
    }
    
    //내 todo 리스트 중 1개 상세보기
    @GetMapping("/{id}")
    public ResponseEntity<?> getTodo(@PathVariable("id") Long todoId, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        LogUtil.debug(this.getClass(), "[TodoController/deleteTodo] userId={}",userId);

        TodoDto.general todo = todoService.getTodo(userId, todoId);
        LogUtil.info(this.getClass(), "[TodoController/todo] get success");

        return ResponseEntity.ok(todo);
    }

    //내 todo 리스트 중 1개 수정하기
    @PutMapping("/{id}")
    public ResponseEntity<?> editTodo(@PathVariable("id") Long todoId, HttpServletRequest request, @RequestBody TodoDto.general todoDto){
        Long userId = (Long) request.getAttribute("userId");
        LogUtil.debug(this.getClass(), "[TodoController/deleteTodo] userId={}",userId);

        todoService.editTodo(userId, todoId, todoDto);
        LogUtil.debug(this.getClass(),"[TodoController/editTodo] edit Success");

        return ResponseEntity.ok(new ResponseDto("edit todo Success"));
    }

    //내 todo 리스트 중 1개 삭제하기
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable("id") Long todoId, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        LogUtil.debug(this.getClass(), "[TodoController/deleteTodo] userId={}",userId);

        todoService.deleteTodo(userId, todoId);
        LogUtil.debug(this.getClass(), "[TodoController/deleteTodo] delete Success");

        return ResponseEntity.ok(new ResponseDto("delete todo Success"));
    }

    //내 todo 검색
    @GetMapping("/search")
    public ResponseEntity<?> searchTodos(@RequestParam(required = false) Boolean completed, @RequestParam(required = false) String keyword, HttpServletRequest request){

        if (completed == null && (keyword == null || keyword.trim().isEmpty())) {
            return ResponseEntity.badRequest().body("completed나 keyword 중 하나는 필수입니다.");
        }

        Long userId = (Long) request.getAttribute("userId");
        LogUtil.debug(this.getClass(), "[TodoController/deleteTodo] userId={}",userId);

        List<TodoDto.general> result = todoService.searchTodos(userId, keyword, completed);

        return ResponseEntity.ok(result);
    }
}

