package com.example.knowledgesphere;

import org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        exclude = {
                OllamaChatAutoConfiguration.class,
                OpenAiEmbeddingAutoConfiguration.class
        }
)
public class KnowledgeSphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeSphereApplication.class, args);
    }
}