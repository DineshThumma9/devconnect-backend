package com.pm.jujutsu.model;

import org.bson.types.ObjectId;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;


@Data
@Builder
public class Notification {

    @Id
    private ObjectId id;
    private String userId;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private long timestamp;

    
}
