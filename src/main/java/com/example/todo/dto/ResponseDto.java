package com.example.todo.dto;

public class ResponseDto {
    //메세지response dto
    private String message;

    public ResponseDto(String message){
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

}
