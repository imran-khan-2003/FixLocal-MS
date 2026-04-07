package com.fixlocal.service;

import com.fixlocal.dto.ChatMessageRequest;
import com.fixlocal.dto.InternalAdminChatStatsDTO;
import com.fixlocal.entity.ChatMessage;
import com.fixlocal.entity.Conversation;

import org.springframework.data.domain.Page;

import java.nio.file.Path;

public interface ChatService {

    Conversation getOrCreateConversation(String bookingId);

    Page<ChatMessage> getMessages(String conversationId, int page, int size);

    ChatMessage sendMessage(String bookingId, String senderEmail, ChatMessageRequest request);

    AttachmentFile getAttachment(String messageId, String requesterEmail);

    InternalAdminChatStatsDTO getAdminChatStats();

    record AttachmentFile(
            Path filePath,
            String fileName,
            String mimeType
    ) {
    }
}
