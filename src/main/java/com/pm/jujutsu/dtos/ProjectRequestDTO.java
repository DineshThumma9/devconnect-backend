package com.pm.jujutsu.dtos;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class ProjectRequestDTO {



    /*
     Same problem as above shoy
    */


    @NotNull
    private String title;


    private String ownerId;



    @NotNull
    private String description;

    @NotNull
    private Set<String> techRequirements = new HashSet<>();

    private boolean isPrivate = false;  // optional: default false

    private String githubLink;  // optional


    private String[] media;

}


