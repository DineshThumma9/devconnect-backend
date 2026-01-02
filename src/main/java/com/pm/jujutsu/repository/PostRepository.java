package com.pm.jujutsu.repository;


import com.pm.jujutsu.model.Post;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends MongoRepository<Post, ObjectId> {
    Optional<Post> getById(String postid);


    List<Post> findAllByOrderByLikesDesc();
    List<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String query, String query2, Pageable pageable);

    List<Post> findAllByTagsContaining(String tag);
}
