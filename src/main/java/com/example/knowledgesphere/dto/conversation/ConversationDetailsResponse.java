package com.example.knowledgesphere.dto.conversation;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDetailsResponse {

    private Long id;

    private String title;

    private List<MessageResponse> messages;

}