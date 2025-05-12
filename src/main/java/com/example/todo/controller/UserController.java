package com.example.todo.controller;

import com.example.todo.entity.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.UserService;
import com.example.todo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
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
    public ResponseEntity<?> me(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        return ResponseEntity.ok(user);
    }
    

}
