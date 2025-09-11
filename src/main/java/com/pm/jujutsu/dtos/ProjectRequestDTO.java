package com.pm.jujutsu.dtos;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Getter
@Setter
public class ProjectRequestDTO {


    @NotNull
    private String title;


    private String ownerId;



    @NotNull
    private String description;

    @NotNull
    private Set<String> techRequirements = new HashSet<>();

    private boolean isPrivate;  // optional: default false

    private String githubLink;  // optional


    private String[] media;

}


