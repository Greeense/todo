package com.example.todo.integration;

import com.example.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.print.attribute.standard.Media;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserAuthIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(UserAuthIntegrationTest.class);
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    // 1) 회원가입-로그인-jwt 인증 흐름 테스트
    // 2) todo생성 -> 목록조회 -> 수정 -> 삭제 테스트
    @Test
    public void userSignupLoginJwtTest() throws Exception {
        // (1) 회원가입
        String sigunupJson = """
                {
                    "username" : "myungji",
                    "email" : "myungji2@gmail.com",
                    "password" : "test1234"
                }
                """;

        mockMvc.perform(post("/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sigunupJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign Success")));

        // (2) 로그인 + JWT토큰 발급
        String loginJson = """
                {
                    "email" : "myungji2@gmail.com",
                    "password" : "test1234"
                }
                """;

        String token = mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replace("{\"access_token\":\"", "")
                .replace("\"}", "");

        // (3) jwt 토큰으로 내 정보 조회
        mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("myungji"))
                .andExpect(jsonPath("$.email").value("myungji2@gmail.com"));

        //  (4) todo생성
        String createTodoJson = """
                {
                    "content" : "할일 1"
                }
                """;

        mockMvc.perform(post("/todos")
                .header("Authorization", "Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createTodoJson))
                .andExpect(status().isOk());

        // (5) todo목록 조회
        mockMvc.perform(get("/todos")
                .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("할일 1"));

        // (6) todo목록 수정
        String updateTodoJson = """
                {
                    "content" : "할일 1 수정",
                    "completed" :  true
                }
                """;

        mockMvc.perform(put("/todos/1")
                .header("Authorization","Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateTodoJson))
                .andExpect(status().isOk());

        // (7) 수정 후 todo 조회(수정된 내용 확인)
        mockMvc.perform(get("/todos/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("할일 1 수정"))
                .andExpect(jsonPath("$.completed").value(true));

        // (8) todo 삭제
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}