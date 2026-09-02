package com.example.knowledgesphere.ai.config;

import com.example.knowledgesphere.ai.tool.CalculatorTool;
import com.example.knowledgesphere.ai.tool.CurrentTimeTool;
import com.example.knowledgesphere.ai.tool.DocumentSearchTool;
import com.example.knowledgesphere.ai.tool.SqlTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    @Bean
    ChatClient chatClient(
            ChatClient.Builder builder,
            CalculatorTool calculatorTool,
            CurrentTimeTool currentTimeTool,
            DocumentSearchTool documentSearchTool,
            SqlTool sqlTool
    ) {

        return builder
                .defaultTools(
                        calculatorTool,
                        currentTimeTool,
                        documentSearchTool,
                        sqlTool
                )
                .build();
    }


}