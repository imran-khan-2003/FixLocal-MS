package com.fixlocal.service.impl;

import com.fixlocal.service.AttachmentStorageService;
import com.fixlocal.service.AttachmentStorageService.StoredAttachment;
import com.fixlocal.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentStorageServiceImpl implements AttachmentStorageService {

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

    public Path resolvePath(ChatMessage.AttachmentMetadata attachment) {
        if (attachment == null) {
            return null;
        }

        if (attachment.getStoragePath() != null && !attachment.getStoragePath().isBlank()) {
            String storagePath = attachment.getStoragePath().trim();
            try {
                if (storagePath.startsWith("file:")) {
                    return Path.of(URI.create(storagePath)).toAbsolutePath().normalize();
                }
                return Paths.get(storagePath).toAbsolutePath().normalize();
            } catch (Exception ignored) {
                // Fall back to fileId-based resolution for legacy/invalid path formats.
            }
        }

        if (attachment.getFileId() == null || attachment.getFileId().isBlank()) {
            return null;
        }

        String extension = "";
        String fileName = attachment.getFileName();
        if (fileName != null) {
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex != -1) {
                extension = fileName.substring(dotIndex);
            }
        }

        Path directory = Paths.get(baseDirectory).toAbsolutePath().normalize();
        return directory.resolve(attachment.getFileId() + extension).normalize();
    }
}
