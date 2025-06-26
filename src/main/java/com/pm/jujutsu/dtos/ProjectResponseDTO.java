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


    public String[] getMedia() {
        return media;
    }

    public void setMedia(String[] media) {
        this.media = media;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTechRequirements() {
        return techRequirements;
    }

    public void setTechRequirements(List<String> techRequirements) {
        this.techRequirements = techRequirements;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerProfilePicUrl() {
        return ownerProfilePicUrl;
    }

    public void setOwnerProfilePicUrl(String ownerProfilePicUrl) {
        this.ownerProfilePicUrl = ownerProfilePicUrl;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getCurrentContributors() {
        return currentContributors;
    }

    public void setCurrentContributors(List<String> currentContributors) {
        this.currentContributors = currentContributors;
    }

    public List<String> getPastContributors() {
        return pastContributors;
    }

    public void setPastContributors(List<String> pastContributors) {
        this.pastContributors = pastContributors;
    }

    public String getGithubLink() {
        return githubLink;
    }

    public void setGithubLink(String githubLink) {
        this.githubLink = githubLink;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
