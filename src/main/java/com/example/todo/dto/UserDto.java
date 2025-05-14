package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class signup{
        @NotBlank(message="이름은 비울 수 없습니다.")
        private String username;
        @NotBlank(message="패스워드는 비울 수 없습니다.")
        private String password;
        @NotBlank(message="이메일은 비울 수 없습니다.")
        private String email;

        private String social;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class login{
        @NotBlank(message="패스워드는 비울 수 없습니다.")
        private String password;
        @NotBlank(message="이메일은 비울 수 없습니다.")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class general {
        private String username;
        private String password;
        private String email;
        private String social;
        private List<TodoDto.general> todos;
    }

}
