package com.example.knowledgesphere.conversation.mapper;

import com.example.knowledgesphere.conversation.dto.*;
import com.example.knowledgesphere.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationMapper {

    public ConversationResponse toResponse(
            Conversation conversation
    ){

        return ConversationResponse.builder()

                .id(conversation.getId())

                .title(conversation.getTitle())

                .updatedAt(conversation.getUpdatedAt())

                .build();

    }

    public MessageResponse toMessage(
            Message message
    ){

        return MessageResponse.builder()

                .id(message.getId())

                .role(message.getRole())

                .content(message.getContent())

                .createdAt(message.getCreatedAt())

                .build();

    }

    public ConversationDetailsResponse toDetails(
            Conversation conversation,
            List<Message> messages
    ){

        return ConversationDetailsResponse.builder()

                .id(conversation.getId())

                .title(conversation.getTitle())

                .messages(
                        messages.stream()
                                .map(this::toMessage)
                                .toList()
                )

                .build();

    }

}