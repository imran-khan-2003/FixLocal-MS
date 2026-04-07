package com.fixlocal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "conversations")
@CompoundIndexes({
        @CompoundIndex(name = "booking_conversation_idx",
                def = "{'bookingId': 1}", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    private String id;

    @Indexed
    private String bookingId;

    @Indexed
    private String userId;

    @Indexed
    private String tradespersonId;

    private ChatMessage lastMessage;

    @Builder.Default
    private int userUnreadCount = 0;

    @Builder.Default
    private int tradespersonUnreadCount = 0;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastMessageAt;
}
