package com.pm.jujutsu.repository;


import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.pm.jujutsu.model.Notification;

public interface NotificationRepository extends MongoRepository<Notification, ObjectId> {


    public Notification findByUserId(String userId);
    public List<Notification> findAllByUserId(String userId);
    public List<Notification> findByUserIdAndIsReadFalse(String userId);
    public long countByUserIdAndIsReadFalse(String userId);

}
