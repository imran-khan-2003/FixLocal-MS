package com.fixlocal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fixlocal.enums.Role;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    @Indexed
    private String bookingId;

    private String senderId;

    private Role senderRole;

    private String content;

    private AttachmentMetadata attachment;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentMetadata {
        private String fileId;
        private String fileName;
        private String mimeType;
        private long sizeBytes;
        private String storagePath;
    }
}
