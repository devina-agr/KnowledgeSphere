package com.example.knowledgesphere.ai.prompt;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final ResourceLoader resourceLoader;

    public String systemPrompt() {

        try {

            Resource resource =
                    resourceLoader.getResource(
                            "classpath:prompts/system.st"
                    );

            return new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new RuntimeException(e);

        }

    }

}