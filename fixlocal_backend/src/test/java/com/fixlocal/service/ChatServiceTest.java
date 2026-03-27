package com.fixlocal.service;

import com.fixlocal.dto.ChatMessageRequest;
import com.fixlocal.exception.ResourceNotFoundException;
import com.fixlocal.model.Booking;
import com.fixlocal.model.ChatMessage;
import com.fixlocal.model.Conversation;
import com.fixlocal.model.Role;
import com.fixlocal.model.User;
import com.fixlocal.repository.BookingRepository;
import com.fixlocal.repository.ChatMessageRepository;
import com.fixlocal.repository.ConversationRepository;
import com.fixlocal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AttachmentStorageService attachmentStorageService;

    @InjectMocks
    private ChatService chatService;

    private Booking booking;
    private User user;

    @BeforeEach
    void setup() {
        booking = Booking.builder()
                .id("booking-1")
                .userId("user-1")
                .tradespersonId("tp-1")
                .build();

        user = User.builder()
                .id("user-1")
                .email("user@mail.com")
                .role(Role.USER)
                .build();
    }

    @Test
    void getOrCreateConversation_createsWhenMissing() {

        when(conversationRepository.findByBookingId("booking-1"))
                .thenReturn(Optional.empty());
        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Conversation conversation = chatService.getOrCreateConversation("booking-1");
        assertThat(conversation.getBookingId()).isEqualTo("booking-1");
    }

    @Test
    void sendMessage_storesAttachmentAndPublishes() throws IOException {

        Conversation conversation = Conversation.builder()
                .id("conv-1")
                .bookingId("booking-1")
                .userId("user-1")
                .tradespersonId("tp-1")
                .build();

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("user@mail.com"))
                .thenReturn(Optional.of(user));
        when(conversationRepository.findByBookingId("booking-1"))
                .thenReturn(Optional.of(conversation));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(attachmentStorageService.store(any()))
                .thenReturn(new AttachmentStorageService.StoredAttachment(
                        "file-1", "note.txt", "/tmp/file-1.txt", "text/plain", 5L));

        ChatMessageRequest request = new ChatMessageRequest();
        request.setContent("Hello there");
        request.setAttachment(new MockMultipartFile(
                "attachment", "note.txt", "text/plain", "data".getBytes()));

        ChatMessage message = chatService.sendMessage(
                "booking-1", "user@mail.com", request);

        assertThat(message.getAttachment()).isNotNull();
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/" + conversation.getId()), any(ChatMessage.class));
    }

    @Test
    void sendMessage_throwsWhenBookingMissing() {
        when(bookingRepository.findById("missing"))
                .thenReturn(Optional.empty());

        ChatMessageRequest request = new ChatMessageRequest();
        request.setContent("hi");

        assertThatThrownBy(() -> chatService.sendMessage("missing", "user@mail.com", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
