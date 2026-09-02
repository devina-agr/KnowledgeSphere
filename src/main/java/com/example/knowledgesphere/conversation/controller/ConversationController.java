package com.example.knowledgesphere.conversation.controller;

import com.example.knowledgesphere.conversation.mapper.ConversationMapper;
import com.example.knowledgesphere.conversation.dto.ConversationDetailsResponse;
import com.example.knowledgesphere.conversation.dto.ConversationResponse;
import com.example.knowledgesphere.conversation.service.ConversationService;
import com.example.knowledgesphere.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService service;

    private final ConversationMapper mapper;

    @GetMapping
    public List<ConversationResponse> getAll(

            @AuthenticationPrincipal
            User user

    ){

        return service.getConversations(user)

                .stream()

                .map(mapper::toResponse)

                .toList();

    }

    @GetMapping("/{id}")
    public ConversationDetailsResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return service.getConversationDetails(id, user);
    }

}