package com.pm.jujutsu.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for real-time chat messages sent via WebSocket
 * Used for both sending and receiving messages through STOMP protocol
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    
    
    // public enum MessageType {
    //     CHAT,           // Regular text message
    //     TYPING,         // User is typing indicator
    //     STOP_TYPING,    // User stopped typing
    //     JOIN,           // User joined conversation
    //     LEAVE           // User left conversation
    // }
    
    private String id;
    private String conversationId;
    private String senderUsername;
    private String recipientUsername;
    private String content;
    // private MessageType type;
    private LocalDateTime timestamp;
    // private boolean read;
}
