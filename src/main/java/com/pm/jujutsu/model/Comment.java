package com.pm.jujutsu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document("comments")
@AllArgsConstructor
@NoArgsConstructor
public class Comment {


    public ObjectId id;
    public String postId;
    public ObjectId userId;
    public String comment;
}
