package com.example.knowledgesphere.ai.service;

import com.example.knowledgesphere.ai.prompt.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final ChatClient chatClient;

    private final PromptService promptService;

    public String chat(String question){

        return chatClient

                .prompt()

                .system(
                        promptService.systemPrompt()
                )

                .user(question)

                .call()

                .content();

    }

}