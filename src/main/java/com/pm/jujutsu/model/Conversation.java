package com.pm.jujutsu.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;


@Data
@Document("conversations")
public class Conversation {
    
    @Id
    private String id;
    private String recipentId;
    private String authorId;
    // private boolean isRead;
    private long timestamp;
    private String lastMessage;
    private List<Message> messages = new ArrayList<>();

}
