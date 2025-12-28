package com.pm.jujutsu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.jujutsu.dtos.ChatMessageDTO;
import com.pm.jujutsu.model.Conversation;
import com.pm.jujutsu.model.Message;
import com.pm.jujutsu.service.ChatService;

import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @GetMapping("/{username}")

    public ResponseEntity<List<Conversation>> getConversationsForUser(@PathVariable String username) {
        return ResponseEntity.ok(chatService.getConversationsForUser(username));
    }




    @GetMapping("/{recipient}/{author}")
    public ResponseEntity<List<Message>> getConversationBetweenUsers(
            @PathVariable String recipient, 
            @PathVariable String author) {
        return ResponseEntity.ok(chatService.getConversationBetweenUsers(recipient, author));
    }

    // ============= WEBSOCKET ENDPOINTS FOR REAL-TIME MESSAGING =============
    
    /**
     * WebSocket endpoint to send a message in real-time
     * 
     * HOW IT WORKS:
     * 1. Client sends message to: /app/chat.sendMessage
     * 2. Server receives it here
     * 3. Server saves to database
     * 4. Server broadcasts to BOTH users via their personal topics:
     *    - /topic/user/{recipientId}
     *    - /topic/user/{senderId}
     * 
     * FRONTEND USAGE:
     * stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
     * 
     * FRONTEND SUBSCRIPTION:
     * stompClient.subscribe("/topic/user/" + userId, (message) => {
     *     // Display new message
     * });
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
 
        
        try {
        

            Message savedMessage = chatService.saveMessage(chatMessage);
            
        
            chatMessage.setTimestamp(savedMessage.getTimestamp());
            
            
            messagingTemplate.convertAndSend(
                "/queue/user/" + chatMessage.getRecipientUsername(), 
                chatMessage
            );
            
           
            messagingTemplate.convertAndSend(
                "/queue/user/" + chatMessage.getSenderUsername(), 
                chatMessage
            );
            
            log.info("Message broadcasted successfully with ID: {}", savedMessage.getId());
            
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            
       
            ChatMessageDTO errorMessage = ChatMessageDTO.builder()
                .content("Failed to send message: " + e.getMessage())
                .senderUsername("SYSTEM")
                .build();
            messagingTemplate.convertAndSend(
                "/queue/user/" + chatMessage.getSenderUsername(), 
                errorMessage
            );
        }
    }
    
    /**
     * WebSocket endpoint for typing indicators
     * 
     * HOW IT WORKS:
     * 1. When user types, frontend sends to: /app/chat.typing
     * 2. Server broadcasts only to recipient: /topic/user/{recipientId}
     * 3. Recipient sees "User is typing..." indicator
     * 
     * FRONTEND USAGE:
     * // When user starts typing
     * stompClient.send("/app/chat.typing", {}, JSON.stringify({
     *     senderId: currentUserId,
     *     recipientId: otherUserId,
     *     type: "TYPING"
     * }));
     * 
     * // When user stops typing (after 2-3 seconds of no typing)
     * stompClient.send("/app/chat.typing", {}, JSON.stringify({
     *     senderId: currentUserId,
     *     recipientId: otherUserId,
     *     type: "STOP_TYPING"
     * }));
     */
    // @MessageMapping("/chat.typing")
    // public void handleTyping(@Payload ChatMessageDTO chatMessage) {
    //     log.info("User {} typing status: {} to {}", 
    //         chatMessage.getSenderUsername(), 
    //         chatMessage.getRecipientUsername());
        
    //     messagingTemplate.convertAndSend(
    //         "/topic/user/" + chatMessage.getRecipientUsername(), 
    //         chatMessage
    //     );
    // }
    
    /**
     * Mark messages as read
     * 
     * HOW IT WORKS:
     * 1. When user opens/views a conversation, frontend sends to: /app/chat.markRead/{conversationId}
     * 2. Server updates all unread messages to read in database
     * 3. Server broadcasts read receipt to sender
     * 
     * FRONTEND USAGE:
     * stompClient.send("/app/chat.markRead/" + conversationId, {}, JSON.stringify({
     *     recipientId: currentUserId,
     *     senderId: otherUserId
     * }));
     */
    // @MessageMapping("/chat.markRead/{conversationId}")
    // public void markMessagesAsRead(
    //         @DestinationVariable String conversationId,
    //         @Payload ChatMessageDTO readReceipt) {
    //     log.info("Marking messages as read in conversation: {}", conversationId);
        
    //     try {
    //         chatService.markConversationAsRead(conversationId, readReceipt.getRecipientUsername());
            
     
    //         messagingTemplate.convertAndSend(
    //             "/topic/user/" + readReceipt.getSenderUsername(),
    //             ChatMessageDTO.builder()
    //                 .conversationId(conversationId)
    //                 .build()
    //         );
    //     } catch (Exception e) {
    //         log.error("Error marking messages as read: {}", e.getMessage(), e);
    //     }
    // }
}
