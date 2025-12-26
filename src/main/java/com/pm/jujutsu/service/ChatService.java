package com.pm.jujutsu.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pm.jujutsu.dtos.ChatMessageDTO;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.model.Conversation;
import com.pm.jujutsu.model.Message;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.ChatRepository;
import com.pm.jujutsu.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatService {



    @Autowired
    public ChatRepository chatRepository;

    @Autowired
    public UserRepository userRepository;


    /**
     * Get all conversations for a user
     */
    public List<Conversation> getConversationsForUser(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        User user = userOpt.get();
        String userId = user.getId().toHexString();
        
        // Find all conversations where user is either author or recipient
        return chatRepository.findByAuthorIdOrRecipentId(userId, userId);
    }

    /**
     * Get conversation between two specific users
     */
    public Conversation getConversationBetweenUsers(String recipientUsername, String authorUsername) {
        Optional<User> recipientOpt = userRepository.findByUsername(recipientUsername);
        Optional<User> authorOpt = userRepository.findByUsername(authorUsername);

        if (!recipientOpt.isPresent() || !authorOpt.isPresent()) {
            throw new UsernameNotFoundException("One or both users not found");
        }

        String recipientId = recipientOpt.get().getId().toHexString();
        String authorId = authorOpt.get().getId().toHexString();
        
        // Find conversation in both directions
        Optional<Conversation> conversation = chatRepository
            .findByAuthorIdAndRecipentIdOrRecipentIdAndAuthorId(
                authorId, recipientId, authorId, recipientId);
        
        return conversation.orElseThrow(() -> 
            new NotFoundException("No conversation found between these users"));
    }

    /**
     * Save a new message via WebSocket
     * This is the core method for real-time messaging
     */
    public Message saveMessage(ChatMessageDTO chatMessageDTO) {
        log.info("Saving message from {} to {}", 
            chatMessageDTO.getSenderId(), 
            chatMessageDTO.getRecipientId());
        
        // Create new message object
        Message newMessage = Message.builder()
            .id(UUID.randomUUID().toString())
            .content(chatMessageDTO.getContent())
            .senderId(chatMessageDTO.getSenderId())
            .timestamp(LocalDateTime.now())
            .read(false)
            .build();
        
        // Find or create conversation
        Optional<Conversation> existingConvo = chatRepository
            .findByAuthorIdAndRecipentIdOrRecipentIdAndAuthorId(
                chatMessageDTO.getSenderId(), 
                chatMessageDTO.getRecipientId(),
                chatMessageDTO.getSenderId(), 
                chatMessageDTO.getRecipientId()
            );
        
        Conversation conversation;
        
        if (existingConvo.isPresent()) {
            // Add message to existing conversation
            conversation = existingConvo.get();
            newMessage.setConversationId(conversation.getId());
            conversation.getMessages().add(newMessage);
            conversation.setLastMessage(chatMessageDTO.getContent());
            conversation.setTimestamp(System.currentTimeMillis());
            conversation.setRead(false); // Mark as unread for recipient
            
            log.info("Added message to existing conversation: {}", conversation.getId());
        } else {
            // Create new conversation
            conversation = new Conversation();
            conversation.setAuthorId(chatMessageDTO.getSenderId());
            conversation.setRecipentId(chatMessageDTO.getRecipientId());
            conversation.setLastMessage(chatMessageDTO.getContent());
            conversation.setTimestamp(System.currentTimeMillis());
            conversation.setRead(false);
            
            // Save conversation first to get ID
            conversation = chatRepository.save(conversation);
            
            newMessage.setConversationId(conversation.getId());
            conversation.getMessages().add(newMessage);
            
            log.info("Created new conversation: {}", conversation.getId());
        }
        
        // Save updated conversation with new message
        chatRepository.save(conversation);
        
        return newMessage;
    }

    /**
     * Mark all messages in a conversation as read
     */
    public void markConversationAsRead(String conversationId, String userId) {
        Optional<Conversation> conversationOpt = chatRepository.findById(conversationId);
        
        if (conversationOpt.isPresent()) {
            Conversation conversation = conversationOpt.get();
            
            // Mark all messages from other user as read
            conversation.getMessages().forEach(message -> {
                if (!message.getSenderId().equals(userId)) {
                    message.setRead(true);
                }
            });
            
            // Update conversation read status
            if (conversation.getRecipentId().equals(userId)) {
                conversation.setRead(true);
            }
            
            chatRepository.save(conversation);
            log.info("Marked conversation {} as read for user {}", conversationId, userId);
        }
    }
}
