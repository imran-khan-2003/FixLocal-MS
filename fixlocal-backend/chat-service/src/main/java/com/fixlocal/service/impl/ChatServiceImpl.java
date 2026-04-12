package com.fixlocal.service.impl;

import com.fixlocal.service.ChatService;
import com.fixlocal.service.AttachmentStorageService;
import com.fixlocal.dto.ChatMessageRequest;
import com.fixlocal.dto.InternalAdminChatStatsDTO;
import com.fixlocal.entity.Booking;
import com.fixlocal.entity.ChatMessage;
import com.fixlocal.entity.Conversation;
import com.fixlocal.enums.Role;
import com.fixlocal.repository.BookingRepository;
import com.fixlocal.repository.ChatMessageRepository;
import com.fixlocal.repository.ConversationRepository;
import com.fixlocal.repository.UserRepository;
import com.fixlocal.exception.ChatException;
import com.fixlocal.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AttachmentStorageService attachmentStorageService;

    public Conversation getOrCreateConversation(String bookingId) {

        return conversationRepository.findByBookingId(bookingId)
                .orElseGet(() -> createConversation(bookingId));
    }

    public Page<ChatMessage> getMessages(String conversationId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    }

    public ChatMessage sendMessage(String bookingId,
                                   String senderEmail,
                                   ChatMessageRequest request) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ChatException(ErrorCode.BOOKING_NOT_FOUND));

        var sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ChatException(ErrorCode.USER_NOT_FOUND));

        if (!booking.getUserId().equals(sender.getId()) &&
                !booking.getTradespersonId().equals(sender.getId())) {
            throw new ChatException(ErrorCode.NOT_PART_OF_BOOKING);
        }

        Conversation conversation = getOrCreateConversation(bookingId);

        ChatMessage.AttachmentMetadata attachmentMetadata = null;
        MultipartFile attachment = request.getAttachment();
        if (attachment != null && !attachment.isEmpty()) {
            attachmentMetadata = storeAttachment(attachment);
        }

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversation.getId())
                .bookingId(bookingId)
                .senderId(sender.getId())
                .senderRole(sender.getRole())
                .content(request.getContent())
                .attachment(attachmentMetadata)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        updateConversation(conversation, saved, sender.getRole());

        return saved;
    }

    private void updateConversation(Conversation conversation,
                                    ChatMessage saved,
                                    Role senderRole) {

        conversation.setLastMessage(saved);
        conversation.setLastMessageAt(LocalDateTime.now());

        if (senderRole == Role.USER) {
            conversation.setTradespersonUnreadCount(
                    conversation.getTradespersonUnreadCount() + 1);
        } else if (senderRole == Role.TRADESPERSON) {
            conversation.setUserUnreadCount(
                    conversation.getUserUnreadCount() + 1);
        }

        conversationRepository.save(conversation);
    }

    private Conversation createConversation(String bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ChatException(ErrorCode.BOOKING_NOT_FOUND));

        Conversation conversation = Conversation.builder()
                .bookingId(bookingId)
                .userId(booking.getUserId())
                .tradespersonId(booking.getTradespersonId())
                .createdAt(LocalDateTime.now())
                .build();

        return conversationRepository.save(conversation);
    }

    private ChatMessage.AttachmentMetadata storeAttachment(MultipartFile attachment) {

        if (attachment.getSize() > 5 * 1024 * 1024) {
            throw new ChatException(ErrorCode.ATTACHMENT_TOO_LARGE);
        }

        try {
            var stored = attachmentStorageService.store(attachment);
            return ChatMessage.AttachmentMetadata.builder()
                    .fileId(stored.fileId())
                    .fileName(stored.originalName())
                    .mimeType(stored.mimeType())
                    .sizeBytes(stored.sizeBytes())
                    .storagePath(stored.storagePath())
                    .build();
        } catch (IOException e) {
            throw new ChatException(ErrorCode.ATTACHMENT_STORAGE_FAILED);
        }
    }

    public AttachmentFile getAttachment(String messageId, String requesterEmail) {
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ChatException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatException(ErrorCode.MESSAGE_NOT_FOUND));

        if (message.getAttachment() == null) {
            throw new ChatException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }

        ChatMessage.AttachmentMetadata attachment = message.getAttachment();

        if (message.getConversationId() == null || message.getConversationId().isBlank()) {
            throw new ChatException(ErrorCode.CONVERSATION_NOT_FOUND);
        }

        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow(() -> new ChatException(ErrorCode.CONVERSATION_NOT_FOUND));

        if (!Objects.equals(requester.getId(), conversation.getUserId())
                && !Objects.equals(requester.getId(), conversation.getTradespersonId())) {
            throw new ChatException(ErrorCode.ATTACHMENT_ACCESS_FORBIDDEN);
        }

        Path filePath = attachmentStorageService.resolvePath(attachment);
        if (filePath == null) {
            throw new ChatException(ErrorCode.ATTACHMENT_METADATA_MISSING);
        }
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new ChatException(ErrorCode.ATTACHMENT_FILE_MISSING);
        }

        String fileName = attachment.getFileName();
        if (fileName == null || fileName.isBlank()) {
            String suffix = attachment.getFileId() != null && !attachment.getFileId().isBlank()
                    ? "-" + attachment.getFileId()
                    : "";
            fileName = "attachment" + suffix;
        }

        return new AttachmentFile(
                filePath,
                fileName,
                attachment.getMimeType()
        );
    }

    public InternalAdminChatStatsDTO getAdminChatStats() {
        return InternalAdminChatStatsDTO.builder()
                .activeConversations(conversationRepository.count())
                .build();
    }
}
