package com.pm.jujutsu.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Document(value = "posts")
public class Post {

    @Id
    private ObjectId id;

    @NotNull
    private ObjectId ownerId;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @CreatedDate
    private Date createdAt;


    private List<String> tags;

    @LastModifiedDate
    private Date updatedAt;

    private String[] media;


    private Set<Comment> comments = new HashSet<>();
    private Set<ObjectId> likedBy = new HashSet<>();
    private Set<ObjectId> sharedBy = new HashSet<>();


    private int likes = 0;


    private int commentsCount = 0;


    private int shares = 0;





}
