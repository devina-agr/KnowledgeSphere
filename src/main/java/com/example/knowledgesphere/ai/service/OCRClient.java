package com.example.knowledgesphere.ai.service;

import com.example.knowledgesphere.ai.model.ExtractionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class OCRClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${vlm.base-url}")
    private String baseUrl;

    public ExtractionResponse extract(
            byte[] fileBytes,
            String fileName
    ) {

        log.info(
                "Sending PDF to Qwen VLM: {} ({} bytes)",
                fileName,
                fileBytes.length
        );

        ByteArrayResource resource =
                new ByteArrayResource(fileBytes) {

                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                };

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add("file", resource);

        try {

            log.info("Calling Qwen VLM: {}/extract", baseUrl);

            ExtractionResponse response =
                    restClientBuilder
                            .baseUrl(baseUrl)
                            .build()
                            .post()
                            .uri("/extract")
                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA
                            )
                            .body(body)
                            .retrieve()
                            .body(ExtractionResponse.class);

            if (response == null) {
                throw new RuntimeException(
                        "Qwen VLM returned empty response"
                );
            }

            log.info(
                    "Qwen VLM response received successfully for {}",
                    fileName
            );

            return response;

        } catch (Exception e) {

            log.error(
                    "Qwen VLM request failed for {}",
                    fileName,
                    e
            );

            throw new RuntimeException(
                    "Failed to communicate with Qwen VLM: "
                            + fileName,
                    e
            );
        }
    }
}