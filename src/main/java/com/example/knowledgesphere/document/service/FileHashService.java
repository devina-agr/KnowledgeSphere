package com.example.knowledgesphere.document.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class FileHashService {

    public String calculateHash(MultipartFile file) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(file.getBytes());

            return HexFormat.of().formatHex(hash);

        }

        catch (Exception e) {

            throw new RuntimeException("Unable to calculate file hash", e);

        }

    }

}