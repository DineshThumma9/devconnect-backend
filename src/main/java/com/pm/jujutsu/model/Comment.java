package com.pm.jujutsu.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document("comments")
public class Comment {


    public ObjectId id;
    public String postId;
    public ObjectId userId;
    public String comment;
}
