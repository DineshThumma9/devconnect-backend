package com.pm.jujutsu.repository;

import com.pm.jujutsu.model.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, ObjectId> {




}
