package com.example.todo.repository;

import com.example.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo,Long> {
     List<Todo> findByUser_Id(Long userId);

     List<Todo> findByUser_IdAndContentContaining(Long userId, String keyword);
     List<Todo> findByUser_IdAndCompleted(Long userid, Boolean completed);
     List<Todo> findByUser_IdAndContentContainingAndCompleted(Long userId, String keyword, Boolean completed);
}
