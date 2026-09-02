package com.example.knowledgesphere.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class VLMClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${vlm.base-url}")
    private String baseUrl;

    public String extract(byte[] imageBytes) {

        ByteArrayResource resource =
                new ByteArrayResource(imageBytes) {

                    @Override
                    public String getFilename() {
                        return "page.png";
                    }
                };

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add("file", resource);

        return restClientBuilder
                .baseUrl(baseUrl)
                .build()
                .post()
                .uri("/extract")
                .contentType(
                        MediaType.MULTIPART_FORM_DATA
                )
                .body(body)
                .retrieve()
                .body(String.class);
    }
}
