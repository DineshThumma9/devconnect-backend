package com.pm.jujutsu.repository;

import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {




    Optional<User> getUserByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> getById(ObjectId id);


    Optional<User> findByUsername(String username);
}