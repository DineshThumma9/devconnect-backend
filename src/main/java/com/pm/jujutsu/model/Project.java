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
import java.util.List;

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
    private List<String> techRequirements = new ArrayList<>();
    private boolean isPrivate = false;
    private boolean isDeleted = false;
    private List<ObjectId> currentContributorIds = new ArrayList<>();
    private List<ObjectId> pastContributorIds = new ArrayList<>();
    @NotNull
    private String status;  // e.g., "active", "completed"
    private String githubLink;


    @CreatedDate
    private LocalDateTime createdAt;


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public @NotNull ObjectId getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(@NotNull ObjectId ownerId) {
        this.ownerId = ownerId;
    }

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

    public List<String> getTechRequirements() {
        return techRequirements;
    }

    public void setTechRequirements(List<String> techRequirements) {
        this.techRequirements = techRequirements;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public List<ObjectId> getCurrentContributorIds() {
        return currentContributorIds;
    }

    public void setCurrentContributorIds(List<ObjectId> currentContributorIds) {
        this.currentContributorIds = currentContributorIds;
    }

    public List<ObjectId> getPastContributorIds() {
        return pastContributorIds;
    }

    public void setPastContributorIds(List<ObjectId> pastContributorIds) {
        this.pastContributorIds = pastContributorIds;
    }

    public @NotNull String getStatus() {
        return status;
    }

    public void setStatus(@NotNull String status) {
        this.status = status;
    }

    public String getGithubLink() {
        return githubLink;
    }

    public void setGithubLink(String githubLink) {
        this.githubLink = githubLink;
    }
}
