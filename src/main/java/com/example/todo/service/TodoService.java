package com.example.todo.service;

import com.example.todo.dto.TodoDto;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.exception.AccessDeniedException;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.exception.UserNotFoundException;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {
    
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    
    //todo 생성 서비스
    public void createTodo(Long userId, TodoDto.create todoDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        System.out.println(">>> userId from request: " + userId);
        Todo todo = Todo.builder()
                .content(todoDto.getContent())
                .completed(false)
                .user(user)
                .build();

        todoRepository.save(todo);
    }
    
    //내 todo 리스트 조회 서비스
    public List<TodoDto.general> getTodos(Long userId){
        List<Todo> todos = todoRepository.findByUser_Id(userId);

        return todos.stream()
                .sorted(Comparator.comparing(Todo::getId))
                .map(todo -> new TodoDto.general(todo.getId(), todo.getContent(), todo.isCompleted()))
                .collect(Collectors.toList());
    }
    
    //내 todo 리스트 중 특정 todo 조회
    public TodoDto.general getTodo(Long userId, Long todoId){
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoNotFoundException("todo not found"));
        //회원의 것이 맞는지 확인
        if(!todo.getUser().getId().equals(userId)){
            throw new AccessDeniedException("Not authorized to Access this todo");
        }

        return new TodoDto.general(todo.getId(), todo.getContent(), todo.isCompleted());
    }

    //내 todo 리스트 중 특정 todo content, completed 수정
    public void editTodo(Long userId, Long todoId, TodoDto.general todoDto){
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoNotFoundException("todo not found"));

        //회원의 것이 맞는지 확인
        if(!todo.getUser().getId().equals(userId)){
            throw new AccessDeniedException("Not authorized to Access this todo");
        }
        //content 변경 시
        if(todoDto.getContent() != null && !todoDto.getContent().equals(todo.getContent())){
            todo.setContent(todoDto.getContent());
        }
        //completed 변경 시
        if(todoDto.isCompleted() != todo.isCompleted()){
            todo.setCompleted(todoDto.isCompleted());
        }

        todoRepository.save(todo);
    }

    //내 todo 리스트 중 특정 todo 삭제
    public void deleteTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new TodoNotFoundException("todo not found"));

        todoRepository.delete(todo);
    }

    //내 todo에서 keyword 검색 결과 조회 서비스
    public List<TodoDto.general> searchTodos(Long userId, String keyword, Boolean completed){
        List<Todo> todos;

        if(keyword != null && completed != null){
            //키워드, 완료여부 고려
            todos = todoRepository.findByUser_IdAndContentContainingAndCompleted(userId, keyword, completed);
        }else if(keyword != null){
            //키워드 고려
            todos = todoRepository.findByUser_IdAndContentContaining(userId, keyword);
        }else if(completed != null){
            //완료여부 고려
            todos = todoRepository.findByUser_IdAndCompleted(userId, completed);
        }else {
            //기타
            todos = todoRepository.findByUser_Id(userId);
        }

        return todos.stream()
                .sorted(Comparator.comparing(Todo::getId))
                .map(todo -> new TodoDto.general(todo.getId(), todo.getContent(), todo.isCompleted()))
                .collect(Collectors.toList());
    }
}
