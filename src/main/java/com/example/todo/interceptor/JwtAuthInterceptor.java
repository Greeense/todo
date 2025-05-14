package com.example.todo.interceptor;

import com.example.todo.repository.UserRepository;
import com.example.todo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Missing or Invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);
        Long userId;
        //유효 JWT토큰 확인
        try {
            userId = jwtUtil.validateTokenAndGetUserId(token);
        }catch (Exception e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid JWT Token");
            return false;
        }

        request.setAttribute("userId", userId); // 이후 컨트롤러에서 사용
        return true;
    }
}