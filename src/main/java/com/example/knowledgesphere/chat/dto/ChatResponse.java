package com.example.knowledgesphere.chat.dto;


import com.example.knowledgesphere.entity.RetrievedDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long conversationId;

    private String response;

    private List<RetrievedDocument> sources;

}