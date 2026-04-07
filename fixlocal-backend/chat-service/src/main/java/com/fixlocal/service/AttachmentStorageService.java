package com.fixlocal.service;

import com.fixlocal.entity.ChatMessage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface AttachmentStorageService {

    StoredAttachment store(MultipartFile file) throws IOException;

    Path resolvePath(ChatMessage.AttachmentMetadata attachment);

    record StoredAttachment(
            String fileId,
            String originalName,
            String storagePath,
            String mimeType,
            long sizeBytes
    ) {
    }
}
