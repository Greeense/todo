package com.example.todo.unit;

import com.example.todo.dto.TodoDto;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    //테스트용
    private final String testName = "test2";
    private final String testEmail = "abcd@gmail.com";
    private final String testPw = "1234";

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    private User user;

    @BeforeEach
    void setup(){
        user = User.builder()
                .id(1L)
                .username(testName)
                .email(testEmail)
                .build();
    }

    //todo생성 테스트
    @Test
    public void createTodo_success() {
        TodoDto.create createDto = new TodoDto.create("test content");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        todoService.createTodo(user.getId(), createDto);
        
        //save된 객체 확인
        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        verify(todoRepository).save(captor.capture());

        Todo savedTodo = captor.getValue();
        assertThat(savedTodo.getContent()).isEqualTo("test content");
        assertThat(savedTodo.isCompleted()).isFalse();
        assertThat(savedTodo.getUser()).isEqualTo(user);
    }

    //todo 리스트 조회 테스트
    @Test
    public void getTodos_success() {
        Todo todo1 = Todo.builder().id(1L).content("task1").completed(false).user(user).build();
        Todo todo2 = Todo.builder().id(1L).content("task2").completed(true).user(user).build();

        when(todoRepository.findByUser_Id(user.getId())).thenReturn(List.of(todo1, todo2));
        
        List<TodoDto.general> result = todoService.getTodos(user.getId());
        
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("task1");
        assertThat(result.get(1).getContent()).isEqualTo("task2");
    }

    //특정 todo 수정 테스트
    @Test
    public void editTodo_success() {
        Long todoId = 1L;
        Todo todo = Todo.builder()
                .id(todoId)
                .content("old Content")
                .completed(false)
                .user(user)
                .build();

        TodoDto.general editDto = new TodoDto.general(todoId, "Updated content", true);

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        todoService.editTodo(user.getId(), todoId, editDto);

        assertThat(todo.getContent()).isEqualTo("Updated content");
        assertThat(todo.isCompleted()).isTrue();
    }

    //특정 todo 삭제 테스트
    @Test
    public void deleteTodo_Success() {
        Long todoId = 1L;
        Todo todo = Todo.builder().id(todoId).content("Delete task").user(user).build();

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(todo));

        todoService.deleteTodo(user.getId(), todoId);

        verify(todoRepository).delete(todo);
    }

    //특정 todo 삭제 시 TodoNotFound 테스트
    @Test
    public void deleteTodo_notfound() {
        Long todoId = 999L;
        when(todoRepository.findById(todoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.deleteTodo(user.getId(), todoId))
                .isInstanceOf(TodoNotFoundException.class);
    }
}
