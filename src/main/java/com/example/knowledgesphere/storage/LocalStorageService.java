package com.example.knowledgesphere.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
public class LocalStorageService {

    @Value("${app.storage.location}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void init() {

        try {

            rootLocation =
                    Paths.get(storageLocation)
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(rootLocation);

            log.info("Storage Location : {}", rootLocation);

        }

        catch (IOException e) {

            throw new RuntimeException(
                    "Unable to initialize storage",
                    e
            );

        }

    }

    public String save(MultipartFile file) {

        try {

            String originalName =
                    StringUtils.cleanPath(
                            file.getOriginalFilename()
                    );

            String filename =
                    UUID.randomUUID()
                            + "_"
                            + originalName;

            Path destination =
                    rootLocation.resolve(filename);

            Files.copy(

                    file.getInputStream(),

                    destination,

                    StandardCopyOption.REPLACE_EXISTING

            );

            log.info("Stored file : {}", destination);

            return destination.toString();

        }

        catch (IOException e) {

            throw new RuntimeException(

                    "Unable to store file",

                    e

            );

        }

    }

    public Resource load(String filePath) {

        try {

            Path path =
                    Paths.get(filePath);

            Resource resource =
                    new UrlResource(path.toUri());

            if (

                    resource.exists()

                            &&

                            resource.isReadable()

            ) {

                return resource;

            }

            throw new RuntimeException(
                    "File not found"
            );

        }

        catch (Exception e) {

            throw new RuntimeException(
                    "Unable to load file",
                    e
            );

        }

    }

    public void delete(String filePath) {

        try {

            Files.deleteIfExists(

                    Paths.get(filePath)

            );

        }

        catch (IOException e) {

            throw new RuntimeException(
                    "Unable to delete file",
                    e
            );

        }

    }

}