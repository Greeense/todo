package com.example.todo.service;

import com.example.todo.entity.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    //회원가입 서비스
    public void signup(User user){

        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new IllegalArgumentException("[UserService/signup] email already exists");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setSocial("local");
        userRepository.save(user);
    }

    //로그인 서비스
    public String login(Map<String, String> loginDto){
        String email = loginDto.get("email");
        String password = loginDto.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("[UserService/login] User not found"));

        if(!BCrypt.checkpw(password, user.getPassword())){
            throw new IllegalArgumentException("[UserService/login] Invalid credentials");
        }

        return jwtUtil.generateToken(user.getId());
    }

    //내 정보 조회 서비스
    public User getUserInfo(Long userId){
        return userRepository.findById(userId).orElseThrow();
    }

    //내 정보 수정 서비스
    public void editUserInfo(Long userId, User newUser){
        //변경 : email, username, password
        Optional<User> userOpt = userRepository.findById(userId);

        if(userOpt.isPresent()){
            //존재 user일때 수정
            User user = userOpt.get();
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
        }else{
            throw new RuntimeException("[UserService/deleteUseInfo] user not found");
        }
    }

    //내 정보 삭제 서비스
    public void deleteUserInfo(Long userId){
        Optional<User> userOpt = userRepository.findById(userId);  //삭제해야하는 user객체 조회

        if(userOpt.isPresent()){
            //삭제할 user 있어?
            User user = userOpt.get();
            userRepository.delete(user);
        }else{
            throw new RuntimeException("[UserService/deleteUseInfo] user not found");
        }
    }
}
