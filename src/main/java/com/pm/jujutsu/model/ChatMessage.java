package com.pm.jujutsu.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessage {
   private String content;
   private String sender;
   private MessageType type;

   public enum MessageType {
      CHAT, JOIN, LEAVE
   }
}
