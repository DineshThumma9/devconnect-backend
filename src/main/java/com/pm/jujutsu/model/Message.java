package com.pm.jujutsu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {


   @Id
   private ObjectId id;

   private String conversationId;
   private String content;
   private String recipientUsername;
   private String senderUsername;
   private LocalDateTime timestamp;
   // private boolean read;
   
}
