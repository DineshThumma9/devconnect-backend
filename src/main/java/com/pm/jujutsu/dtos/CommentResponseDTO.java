package com.pm.jujutsu.dtos;

import lombok.Data;
import java.util.Date;

@Data
public class CommentResponseDTO {

    private String id;
    private String comment;
    private String username;
    private String userProfilePicUrl;
    private Date createdAt;
}
