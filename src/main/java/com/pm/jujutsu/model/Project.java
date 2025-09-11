package com.pm.jujutsu.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Document(value = "projects")
public class Project {

    @Id
    private ObjectId id;

    @NotNull
    private ObjectId ownerId;

    @NotNull
    @Indexed(unique = true)
    private String title;


    @NotNull
    private String description;


    private Set<String> techRequirements = new HashSet<>();

    private boolean isPrivate = false;





    private Set<ObjectId> currentContributorIds = new HashSet<>();
    private Set<ObjectId> pastContributorIds = new HashSet<>();



    @NotNull
    private String status;  // e.g., "active", "completed"


    private String githubLink;




    @CreatedDate
    private LocalDateTime createdAt;


}
