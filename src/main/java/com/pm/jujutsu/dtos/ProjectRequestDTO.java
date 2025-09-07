package com.pm.jujutsu.dtos;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class ProjectRequestDTO {


    @NotNull
    private String title;


    private String ownerId;

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @NotNull
    private String description;

    @NotNull
    private List<String> techRequirements;

    private boolean isPrivate;  // optional: default false

    private String githubLink;  // optional


    private String[] media;

}


