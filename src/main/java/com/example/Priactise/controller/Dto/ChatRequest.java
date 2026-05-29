package com.example.Priactise.controller.Dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;
    private String message;
}
