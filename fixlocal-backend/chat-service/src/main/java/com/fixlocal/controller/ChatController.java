package com.fixlocal.controller;

import com.fixlocal.dto.ChatMessageRequest;
import com.fixlocal.exception.ErrorCode;
import com.fixlocal.exception.ChatException;
import com.fixlocal.entity.ChatMessage;
import com.fixlocal.entity.Conversation;
import com.fixlocal.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations/{bookingId}")
    public ResponseEntity<Conversation> getConversation(@PathVariable String bookingId) {
        return ResponseEntity.ok(chatService.getOrCreateConversation(bookingId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Page<ChatMessage>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(chatService.getMessages(conversationId, page, size));
    }

    @PostMapping("/bookings/{bookingId}/messages")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable String bookingId,
            Authentication authentication,
            @Valid @ModelAttribute ChatMessageRequest request) {

        return ResponseEntity.ok(
                chatService.sendMessage(bookingId, authentication.getName(), request));
    }

    @GetMapping("/messages/{messageId}/attachment")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable String messageId,
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            throw new ChatException(ErrorCode.UNAUTHORIZED);
        }

        ChatService.AttachmentFile attachmentFile = chatService.getAttachment(messageId, authentication.getName());
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(attachmentFile.filePath());
        } catch (IOException ex) {
            throw new ChatException(ErrorCode.ATTACHMENT_FILE_MISSING);
        }
        Resource resource = new ByteArrayResource(bytes);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (attachmentFile.mimeType() != null && !attachmentFile.mimeType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(attachmentFile.mimeType());
            } catch (Exception ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        String safeFileName = attachmentFile.fileName() == null || attachmentFile.fileName().isBlank()
                ? "attachment"
                : attachmentFile.fileName();

        String encodedFileName = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(bytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }
}
