package com.example.todo.unit;

import com.example.todo.dto.UserDto;
import com.example.todo.entity.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.service.UserService;
import com.example.todo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    //테스트용
    private final String testName = "test2";
    private final String testEmail = "abcd@gmail.com";
    private final String testPw = "1234";
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    public void setup() {
        reset(userRepository, jwtUtil);

        user = User.builder()
                .id(1L)
                .username(testName)
                .email(testEmail)
                .password(BCrypt.hashpw(testPw,BCrypt.gensalt()))
                .social("local")
                .build();
    }

    //로그인 , jwt인증 , jwt반환 테스트
    @Test
    public void login_validCredentials_ReturnToken() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getId())).thenReturn("dummy.jwt.token");
        
        UserDto.login loginDto = new UserDto.login();
        loginDto.setEmail(testEmail);
        loginDto.setPassword(testPw);
        
        String token = userService.login(loginDto);
        
        assertNotNull(token);
        assertEquals("dummy.jwt.token",token);
        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(jwtUtil, times(1)).generateToken(user.getId());
    }

    // 로그인 실패 테스트
    @Test
    public void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

        UserDto.login loginDto = new UserDto.login();
        loginDto.setEmail(testEmail);
        loginDto.setPassword("wrong_password");

        assertThrows(IllegalArgumentException.class, () -> userService.login(loginDto));
    }

    //회원가입 테스트
    @Test
    public void signup_NewUser_SaveUser() {
        UserDto.signup newUserDto = new UserDto.signup();

        newUserDto.setUsername("newuser");
        newUserDto.setPassword("test12345678");
        newUserDto.setEmail("newuser@example.com");
        newUserDto.setSocial("local");

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());

        userService.signup((newUserDto));

        verify(userRepository, times(1)).save(any(User.class));
    }

    //이미 존재 회원 테스트
    @Test
    public void signup_UsernameExists_ThrowsException() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

        UserDto.signup sameUserDto = new UserDto.signup();
        sameUserDto.setUsername(testName);
        sameUserDto.setPassword(testPw);
        sameUserDto.setEmail(testEmail);
        sameUserDto.setSocial("local");

        assertThrows(IllegalArgumentException.class, () -> userService.signup(sameUserDto));

        verify(userRepository, times(1)).findByEmail(testEmail);
    }
}
