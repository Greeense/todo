package com.example.todo.controller;

import com.example.todo.dto.ResponseDto;
import com.example.todo.dto.TodoDto;
import com.example.todo.dto.UserDto;
import com.example.todo.entity.User;
import com.example.todo.service.UserService;
import com.example.todo.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping ("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid UserDto.signup signupDto){
        userService.signup(signupDto);
        LogUtil.info(this.getClass(),"[UserController/signup] success");
        return ResponseEntity.ok(new ResponseDto("Sign Success"));
    }
    
    //로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserDto.login loginDto){
        String token = userService.login(loginDto);
        LogUtil.debug(this.getClass(),"[UserController/login] success");
        return ResponseEntity.ok(Collections.singletonMap("access_token",token));
    }
    
    //내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserDto.general> searchUserInfo(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        UserDto.general userResponse = userService.getUserInfo(userId);

        LogUtil.debug(this.getClass(),"[UserController/me] get success");

        return ResponseEntity.ok(userResponse);
    }

    //내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<?> editUserInfo(@RequestBody User user, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");

        userService.editUserInfo(userId, user);
        LogUtil.debug(this.getClass(),"[UserController/me] edit success");

        return ResponseEntity.ok(new ResponseDto("edit userInfo Success"));
    }

    //회원 삭제
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUserInfo(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");

        userService.deleteUserInfo(userId);
        LogUtil.debug(this.getClass(),"[UserController/deleteUserInfo] delete user Success");

        return ResponseEntity.ok(new ResponseDto("delete user Success"));
    }
}
