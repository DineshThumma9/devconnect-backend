package com.pm.jujutsu.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class ProjectResponseDTO {



    private String id;
    private String title;
    private String description;
    private List<String> techRequirements;
    private String ownerUsername;
    private String ownerProfilePicUrl;
    private boolean isPrivate;
    private String status;
    private String ownerId;
    private List<String> currentContributors;
    private List<String> pastContributors;
    private String githubLink;
    private Date createdAt;
    private String[] media;




}
