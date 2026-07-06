package com.example.knowledgesphere.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private Long conversationId;

    @NotBlank
    private String message;

}