package com.example.todo.service;

import com.example.todo.dto.TodoDto;
import com.example.todo.dto.UserDto;
import com.example.todo.entity.User;
import com.example.todo.exception.UserNotFoundException;
import com.example.todo.repository.UserRepository;
import com.example.todo.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    //회원가입 서비스
    public void signup(UserDto.signup userDto){
        if(userRepository.findByEmail(userDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("email already exists");
        }

        User user = User.builder()
                    .username(userDto.getUsername())
                    .password(BCrypt.hashpw(userDto.getPassword(), BCrypt.gensalt()))
                    .email(userDto.getEmail())
                    .social("local")
                    .build();

        userRepository.save(user);
    }

    //로그인 서비스
    public String login(UserDto.login userDto){
        String email = userDto.getEmail();
        String password = userDto.getPassword();

        //유저 존재 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        //암호 틀림
        if(!BCrypt.checkpw(password, user.getPassword())){
            throw new IllegalArgumentException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getId());
    }

    //내 정보 조회 서비스
    public UserDto.general getUserInfo(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<TodoDto.general> todoDtos = user.getTodos().stream()
                .map(todo -> new TodoDto.general(todo.getId(), todo.getContent(), todo.isCompleted()))
                .toList();

        return new UserDto.general(
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getSocial(),
                todoDtos
        );
    }

    //내 정보 수정 서비스
    public void editUserInfo(Long userId, User newUser){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        //userName 변경시
        if(newUser.getUsername() != null && !newUser.getUsername().isEmpty() &&
                !user.getUsername().equals(newUser.getUsername())){
            user.setUsername(newUser.getUsername());
        }
        //Email 변경시
        if(newUser.getEmail() != null && !newUser.getEmail().isEmpty() &&
                !user.getEmail().equals(newUser.getEmail())){
            user.setEmail(newUser.getEmail());
        }
        //password 변경 시
        if(newUser.getPassword() != null && !newUser.getPassword().isEmpty()){
            boolean pwMatched = BCrypt.checkpw(newUser.getPassword(), user.getPassword());
            if(!pwMatched){
                user.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
            }
        }

        userRepository.save(user);
    }

    //내 정보 삭제 서비스
    public void deleteUserInfo(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);
    }
}
