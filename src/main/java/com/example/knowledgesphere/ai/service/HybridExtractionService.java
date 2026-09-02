package com.example.knowledgesphere.ai.service;

import com.example.knowledgesphere.ai.model.ExtractionResponse;
import com.example.knowledgesphere.ai.model.StructuredPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HybridExtractionService {

    private final OCRClient ocrClient;

    public List<StructuredPage> extract(
            byte[] fileBytes,
            Long documentId,
            String fileName
    ) {

        log.info(
                "Processing document using Qwen VLM: {}",
                fileName
        );

        try {

            // Send PDF to Qwen VLM through OCRClient
            ExtractionResponse response =
                    ocrClient.extract(
                            fileBytes,
                            fileName
                    );

            // Validate response
            if (response == null) {

                throw new RuntimeException(
                        "Qwen VLM returned null response for "
                                + fileName
                );
            }

            if (response.getPages() == null ||
                    response.getPages().isEmpty()) {

                throw new RuntimeException(
                        "Qwen VLM returned no pages for "
                                + fileName
                );
            }

            log.info(
                    "Qwen VLM extraction successful: {} pages extracted from {}",
                    response.getPages().size(),
                    fileName
            );

            return response.getPages();

        } catch (Exception e) {

            log.error(
                    "Qwen VLM extraction failed for {}",
                    fileName,
                    e
            );

            throw new RuntimeException(
                    "Failed to extract PDF using Qwen VLM: "
                            + fileName,
                    e
            );
        }
    }
}