package com.pm.jujutsu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {

   private String id;
   private String conversationId;
   private String content;
   private String senderId;
   private LocalDateTime timestamp;
   // private boolean read;
   
}
