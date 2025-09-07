package com.pm.jujutsu.model;

import com.pm.jujutsu.dtos.UserResponseDTO;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document("comments")
public class Comment {


    public String commentId;
    public String postId;
    public UserResponseDTO user;
    public String comment;
}
