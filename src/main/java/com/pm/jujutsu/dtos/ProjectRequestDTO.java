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


    public @NotNull String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    public @NotNull List<String> getTechRequirements() {
        return techRequirements;
    }

    public void setTechRequirements(@NotNull List<String> techRequirements) {
        this.techRequirements = techRequirements;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getGithubLink() {
        return githubLink;
    }

    public void setGithubLink(String githubLink) {
        this.githubLink = githubLink;
    }
}
