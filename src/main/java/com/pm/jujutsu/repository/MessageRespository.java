


package com.pm.jujutsu.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.pm.jujutsu.model.Message;

public interface MessageRespository extends MongoRepository<Message,ObjectId> {


    Optional<List<Message>> findByConversationIdOrderByTimestamp(ObjectId hexString);


    
}