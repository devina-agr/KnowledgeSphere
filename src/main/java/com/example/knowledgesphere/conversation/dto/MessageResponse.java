package com.example.knowledgesphere.conversation.dto;

import com.example.knowledgesphere.entity.MessageRole;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;

    private MessageRole role;

    private String content;

    private LocalDateTime createdAt;

}