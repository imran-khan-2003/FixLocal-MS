package com.fixlocal.controller;

import com.fixlocal.dto.ChatMessageRequest;
import com.fixlocal.model.ChatMessage;
import com.fixlocal.model.Conversation;
import com.fixlocal.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
