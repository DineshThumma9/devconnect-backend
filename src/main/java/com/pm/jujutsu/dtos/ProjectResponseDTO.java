package com.pm.jujutsu.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
public class ProjectResponseDTO {



    private String id;
    private String title;
    private String description;
    private Set<String> techRequirements = new HashSet<>();
    private String ownerUsername;
    private String ownerProfilePicUrl;
    private boolean isPrivate;
    private String status;
    private String ownerId;
    private Set<String> currentContributors = new HashSet<>();
    private String githubLink;
    private Date createdAt;
    private String[] media;




}
