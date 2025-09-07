package com.pm.jujutsu.dtos;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class PostResponseDTO {
    // Post ID

    private String id;
    private String title;
    private String content;
    private String ownerUsername;  // OR full owner object if needed
    private String ownerProfilePicUrl;
    private Date createdAt;
    private Date updatedAt;
    private int likes;
    private int comments;
    private int shares;
    private String[] media;


}
