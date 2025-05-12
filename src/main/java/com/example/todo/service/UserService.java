package com.example.todo.service;

import com.example.todo.entity.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    //회원가입 서비스
    public void signup(User user){

        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new IllegalArgumentException("[UserService/signup] Username already exists");
        }
        System.out.println("signup");
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setSocial("local");
        userRepository.save(user);
    }

    //로그인 서비스
    public String login(Map<String, String> loginDto){
        String userName = loginDto.get("username");
        String password = loginDto.get("password");

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new IllegalArgumentException("[UserService/login] User not found"));

        if(!BCrypt.checkpw(password, user.getPassword())){
            throw new IllegalArgumentException("[UserService/login] Invalid credentials");
        }

        return jwtUtil.generateToken(user.getId());
    }

    public User getUserInfo(Long userId){
        return userRepository.findById(userId).orElseThrow();
    }
}
