package com.pm.jujutsu.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pm.jujutsu.dtos.ChatMessageDTO;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.model.Conversation;
import com.pm.jujutsu.model.Message;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.ChatRepository;
import com.pm.jujutsu.repository.MessageRespository;
import com.pm.jujutsu.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatService {

    @Autowired
    public ChatRepository chatRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public MessageRespository messageRespository;


    private final static String CACHE_CHAT = "chatCache";

    @Cacheable(value = CACHE_CHAT, key = "#username")
    public List<Conversation> getConversationsForUser(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        User u = userOpt.get();
        String user = u.getUsername();
        
        return chatRepository.findByAuthorUsernameOrRecipentUsername(user);
    }




    @Cacheable(value = CACHE_CHAT, key = "#recipientUsername + '_' + #authorUsername")
    public List<Message> getConversationBetweenUsers(String recipientUsername, String authorUsername) {
        Optional<User> recipientOpt = userRepository.findByUsername(recipientUsername);
        Optional<User> authorOpt = userRepository.findByUsername(authorUsername);

        if (!recipientOpt.isPresent() || !authorOpt.isPresent()) {
            throw new UsernameNotFoundException("One or both users not found");
        }

        String recipient = recipientOpt.get().getUsername();
        String author = authorOpt.get().getUsername();
        
        Optional<Conversation> conversation = chatRepository
            .findByAuthorUsernameAndRecipentUsernameOrRecipentUsernameAndAuthorUsername(
                author, recipient, author, recipient);

        if(!conversation.isPresent()) {
            log.info("No conversation found between {} and {}", author, recipient);
            return List.of();
        }

        Optional<List<Message>> convo = messageRespository
            .findByConversationIdOrderByTimestamp(conversation.get().getId().toHexString());
        
        log.info("Found {} messages in conversation between {} and {}", 
            convo.map(List::size).orElse(0), author, recipient);

        log.info("Messages: {}", convo.orElse(List.of()).stream()
            .limit(5)
            .map(Message::toString)
            .toList()
        );

        return convo.orElse(List.of());
    }

    public Message saveMessage(ChatMessageDTO chatMessageDTO) {
        log.info("Saving message from {} to {}", 
            chatMessageDTO.getSenderUsername(), 
            chatMessageDTO.getRecipientUsername());
        
        // Create message WITHOUT id - MongoDB will auto-generate it
        Message newMessage = Message.builder()
            .content(chatMessageDTO.getContent())
            .senderUsername(chatMessageDTO.getSenderUsername())
            .recipientUsername(chatMessageDTO.getRecipientUsername())  // ADD THIS LINE
            .timestamp(LocalDateTime.now())
            .build();
        
        Optional<Conversation> existingConvo = chatRepository
            .findByAuthorUsernameAndRecipentUsernameOrRecipentUsernameAndAuthorUsername(
                chatMessageDTO.getSenderUsername(), 
                chatMessageDTO.getRecipientUsername(),
                chatMessageDTO.getSenderUsername(), 
                chatMessageDTO.getRecipientUsername()
            );
        
        Conversation conversation;
        
        if (existingConvo.isPresent()) {
            conversation = existingConvo.get();
            newMessage.setConversationId(conversation.getId().toHexString());
            conversation.setLastMessage(chatMessageDTO.getContent());
            conversation.setTimestamp(System.currentTimeMillis());
            
            log.info("Added message to existing conversation: {}", conversation.getId());
        } else {
            conversation = new Conversation();
            conversation.setAuthorUsername(chatMessageDTO.getSenderUsername());
            conversation.setRecipentUsername(chatMessageDTO.getRecipientUsername());
            conversation.setLastMessage(chatMessageDTO.getContent());
            conversation.setTimestamp(System.currentTimeMillis());
            
            conversation = chatRepository.save(conversation);
            newMessage.setConversationId(conversation.getId().toHexString());
            
            log.info("Created new conversation: {}", conversation.getId());
        }
        
        // IMPORTANT: Save message FIRST, then save conversation
        Message savedMessage = messageRespository.save(newMessage);
        chatRepository.save(conversation);
        
        // CHANGE: Return savedMessage (with ID) instead of newMessage (without ID)
        return savedMessage;
    }
}