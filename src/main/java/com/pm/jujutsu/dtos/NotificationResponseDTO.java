package com.pm.jujutsu.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponseDTO {



    public String id;
    public String userName;
    public String type;
    public String message;
    public boolean isRead;
    public long timestamp;
    
    
}
