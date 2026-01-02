package com.pm.jujutsu.repository;


import com.pm.jujutsu.model.Project;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public  interface ProjectRepository extends MongoRepository<Project, ObjectId> {


    @Query("SELECT * FROM projects WHERE project.title === {title}")
    List<Optional<Project>> findAllByTitle(String title);



    List<Optional<Project>> findAllByDescription();


    List<Optional<Project>> findAllByTechRequirementsIsContaining();



    List<Project> findAllByCurrentContributorIds(ObjectId userId);



    List<Project> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String query, String query2, Pageable pageable);



    List<Project> findAllByCurrentContributorIdsContains(ObjectId objectId);
}
