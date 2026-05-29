package com.example.Priactise.controller;

import com.example.Priactise.controller.Dto.ApiResponse;
import com.example.Priactise.controller.Dto.ChatRequest;
import com.example.Priactise.controller.Dto.ChatResponse;
import com.example.Priactise.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ConversationService conversationService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = conversationService.processMessage(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Something went wrong: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
