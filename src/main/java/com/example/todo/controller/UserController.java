package com.example.todo.controller;

import com.example.todo.entity.User;
import com.example.todo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping ("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user){
        try {
            userService.signup(user);
            return ResponseEntity.ok("[UserController/signup] Signup Success");
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    
    //로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginDto){
        try {
            String token = userService.login(loginDto);
            return ResponseEntity.ok(Collections.singletonMap("access_token",token));
        } catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    
    //내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> searchUserInfo(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        System.out.println("[UserController/me] success");
        return ResponseEntity.ok(user);
    }

    //내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<?> editUserInfo(@RequestBody User user, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        userService.editUserInfo(userId, user);
        return ResponseEntity.ok("[UserController/user] edit userInfo Success");
    }

    //회원 삭제
    @DeleteMapping("/me")
    public void deleteUserInfo(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        userService.deleteUserInfo(userId);
        System.out.println("[UserController/deleteUserInfo] delete userInfo Success");
    }
}
