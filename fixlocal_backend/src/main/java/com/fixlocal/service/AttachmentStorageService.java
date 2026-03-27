package com.fixlocal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentStorageService {

    @Value("${chat.attachments.directory:chat_attachments}")
    private String baseDirectory;

    public StoredAttachment store(MultipartFile file) throws IOException {

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex != -1) {
            extension = originalName.substring(dotIndex);
        }

        String fileId = UUID.randomUUID().toString();
        String fileName = fileId + extension;

        Path directory = Paths.get(baseDirectory).toAbsolutePath().normalize();
        Files.createDirectories(directory);

        Path target = directory.resolve(fileName);
        Files.copy(file.getInputStream(), target);

        return new StoredAttachment(fileId,
                originalName,
                target.toString(),
                file.getContentType(),
                file.getSize());
    }

    public record StoredAttachment(
            String fileId,
            String originalName,
            String storagePath,
            String mimeType,
            long sizeBytes
    ) {}
}
