package com.example.knowledgesphere.ai.service;

import com.example.knowledgesphere.ai.model.ExtractionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoclingClient {

    private final RestClient restClient;

    @Value("${docling.base-url}")
    private String baseUrl;

    public ExtractionResponse extract(byte[] pdf, String fileName) throws IOException {

        log.info("Calling Docling for file: {}", fileName);
        log.info("Docling URL: {}/extract", baseUrl);

        MultipartBodyBuilder body = new MultipartBodyBuilder();

        body.part(
                "file",
                new ByteArrayResource(pdf) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                });

        try {

            ExtractionResponse response = restClient.post()
                    .uri(baseUrl + "/extract")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body.build())
                    .retrieve()
                    .body(ExtractionResponse.class);

            if (response == null) {
                log.error("Docling returned NULL response.");
                throw new RuntimeException("Docling returned null response.");
            }

            log.info("Docling response received successfully.");

            if (response.getPages() == null) {
                log.warn("Pages list is null.");
            } else {
                log.info("Pages extracted: {}", response.getPages().size());

                response.getPages().forEach(page -> {
                    log.info("Page {} -> {} blocks",
                            page.getPageNumber(),
                            page.getBlocks() == null ? 0 : page.getBlocks().size());
                });
            }

            return response;

        } catch (Exception ex) {
            log.error("Docling request failed.", ex);
            throw ex;
        }
    }
}