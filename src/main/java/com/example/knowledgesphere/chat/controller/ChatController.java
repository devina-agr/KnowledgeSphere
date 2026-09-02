package com.example.knowledgesphere.chat.controller;

import com.example.knowledgesphere.chat.dto.ChatRequest;
import com.example.knowledgesphere.chat.dto.ChatResponse;
import com.example.knowledgesphere.chat.service.ChatService;
import com.example.knowledgesphere.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse chat(

            @RequestBody
            @Valid
            ChatRequest request,

            @AuthenticationPrincipal User user

    ) {

        return chatService.ask(
                request.getMessage(),
                request.getConversationId(),
                user
        );

    }

}