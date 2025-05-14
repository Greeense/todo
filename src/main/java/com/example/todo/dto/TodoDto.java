package com.example.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class TodoDto {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class create{
        @NotBlank(message = "할 일 내용은 비울 수 없습니다.")
        private String content;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class general{
        private Long id;
        private String content;
        private boolean completed;
    }


}
