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
    private String recipentUsername;
    private String authorUsername;
    private long timestamp;
    private String lastMessage;


}
