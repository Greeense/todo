package com.example.todo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
//JWT 토큰 발급 시 userId를 담아 만들고, 검증 시 서명/만료 검증 후 userId를 꺼내주는 util
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY_STRING;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // 문자열을 안전한 SecretKey로 변환
        this.secretKey = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
    }
    
    //userID 기반 jwt 액세스 토큰 발급
    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date()) //발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 만료시간 :  1시간
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY_STRING)//secret_key 사용하여 signature 값 생성
                .compact(); //JWT 문자열로 변환
    }
    
    //JWT 검증 후 사용자 ID 반환
    public Long validateTokenAndGetUserId(String token){
        try {
            Claims claims = Jwts.parser() //JWT 서명 검증 시 secret_key 설정
                    .setSigningKey(SECRET_KEY_STRING) //toekn 서명 검증하고 내용 추출
                    .parseClaimsJws(token) //내용을 반환
                    .getBody(); //내용에서 userId를 가져옴

            System.out.println("[JwtUtil/validateTokenAndGetUserId] validate Token and Get userId Success");
            return Long.parseLong(claims.getSubject()); //userId를 long으로 변환 후 반환
        }catch (Exception e){
            throw new RuntimeException("Invalid jwt Token");
        }
    }
}
