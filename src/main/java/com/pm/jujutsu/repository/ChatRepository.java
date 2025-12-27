package com.pm.jujutsu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pm.jujutsu.model.Conversation;
import com.pm.jujutsu.model.Message;

/**
 * Repository for managing chat conversations in MongoDB
 * Uses Spring Data MongoDB's query derivation from method names
 */
@Repository
public interface ChatRepository extends MongoRepository<Conversation, String> {

    /**
     * Find all conversations where user is either author or recipient
     * Used to get all chats for a user
     */
    // List<Conversation> findByAuthorIdOrRecipentId(String authorId, String recipentId);
    
    /**
     * Find specific conversation between two users (bidirectional)
     * Checks both author->recipient and recipient->author
     */
    // Optional<Conversation> findByAuthorIdAndRecipentIdOrRecipentIdAndAuthorId(
    //     String authorId1, String recipentId1, 
    //     String recipentId2, String authorId2
    // );

    Optional<Conversation> findByAuthorUsernameAndRecipentUsernameOrRecipentUsernameAndAuthorUsername(
            String senderUsername, String recipientUsername, String senderUsername2, String recipientUsername2);

//   Optional<List<Message>> findMessageWithConversationId(Conversation list);

    List<Conversation> findByAuthorUsernameOrRecipentUsername(String username, String username2);

    //Optional<List<Message>> findMessageWithConversationId(Conversation list);
}

