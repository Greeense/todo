package com.example.todo.service;

import com.example.todo.dto.TodoDto;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {
    
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    
    //todo 생성
    public void createTodo(Long userId, TodoDto dto) {
        User user = userRepository.findById(userId).orElseThrow();
        Todo todo = Todo.builder()
                .title(dto.getTitle())
                .user(user)
                .build();
        
        todoRepository.save(todo);
    }
    
    //todo 조회 - 사용자의 todo 조회
    public List<Todo> getTodos(Long userId){
        return todoRepository.findByUser_Id(userId);
    }
    
    //todo 조회 - 검색
    public List<Todo> searchTodos(Long userId, String keyword){
        return todoRepository.findByUser_IdAndTitleContaining(userId, keyword);
    }
}
