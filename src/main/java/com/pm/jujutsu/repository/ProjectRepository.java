package com.pm.jujutsu.repository;


import com.pm.jujutsu.model.Project;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public  interface ProjectRepository extends MongoRepository<Project, ObjectId> {

}
