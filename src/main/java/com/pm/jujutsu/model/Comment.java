package com.pm.jujutsu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Data
@Document("comments")
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    
    @Id
    public ObjectId id;

    
    public ObjectId postId;
    public ObjectId userId;
    public String comment;
    public Date createdAt = new Date();
}
