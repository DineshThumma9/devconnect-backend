package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.ProjectRequestDTO;
import com.pm.jujutsu.dtos.ProjectResponseDTO;
import com.pm.jujutsu.mappers.ProjectMapper;
import com.pm.jujutsu.model.Project;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.ProjectRepository;
import com.pm.jujutsu.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMapper projectMapper;



    public ProjectResponseDTO createProject(String ownerId,ProjectRequestDTO projectRequestDTO) {
        // First make sure the ownerId is set in the request or assign a default if needed
        Project project = projectMapper.toEntity(projectRequestDTO);

        // Make sure ownerId is properly set before saving
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }

        Project savedProject = projectRepository.save(project);
        ProjectResponseDTO projectResponseDTO = projectMapper.toResponseEntity(savedProject);

        // Add null check before looking up the owner
        if (savedProject.getOwnerId() != null) {
            Optional<User> owner = userRepository.findById(savedProject.getOwnerId());
            if (owner.isPresent()) {
                projectResponseDTO.setOwnerUsername(owner.get().getUsername());
                projectResponseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());

                // Process current contributors if they exist
                if (savedProject.getCurrentContributorIds() != null && !savedProject.getCurrentContributorIds().isEmpty()) {
                    List<String> currentContributorNames = savedProject.getCurrentContributorIds().stream()
                            .filter(id -> id != null) // Skip any null IDs
                            .map(id -> userRepository.findById(id))
                            .filter(Optional::isPresent)
                            .map(user -> user.get().getUsername())
                            .collect(Collectors.toList());
                    projectResponseDTO.setCurrentContributors(currentContributorNames);
                }

                // Process past contributors if they exist
                if (savedProject.getPastContributorIds() != null && !savedProject.getPastContributorIds().isEmpty()) {
                    List<String> pastContributorNames = savedProject.getPastContributorIds().stream()
                            .filter(id -> id != null) // Skip any null IDs
                            .map(id -> userRepository.findById(id))
                            .filter(Optional::isPresent)
                            .map(user -> user.get().getUsername())
                            .collect(Collectors.toList());
                    projectResponseDTO.setPastContributors(pastContributorNames);
                }
            }
        }

        return projectResponseDTO;
    }

    public ProjectResponseDTO updateProject(String projectId, ProjectRequestDTO projectRequestDTO) {
        ObjectId objectId = new ObjectId(projectId);
        Project project = projectRepository.findById(objectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        project.setTitle(projectRequestDTO.getTitle());
        project.setDescription(projectRequestDTO.getDescription());
        project.setTechRequirements(projectRequestDTO.getTechRequirements());
        project.setPrivate(projectRequestDTO.isPrivate());
        project.setGithubLink(projectRequestDTO.getGithubLink());

        Project savedProject = projectRepository.save(project);
        ProjectResponseDTO responseDTO = projectMapper.toResponseEntity(savedProject);

        // Add owner and contributor details like in createProject
        Optional<User> owner = userRepository.findById(savedProject.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());

            // Add contributor details similarly to createProject
            // (same code as in createProject for contributors)
            List<String> currentContributorNames = new ArrayList<>();
            if (savedProject.getCurrentContributorIds() != null && !savedProject.getCurrentContributorIds().isEmpty()) {
                currentContributorNames = savedProject.getCurrentContributorIds().stream()
                        .map(id -> userRepository.findById(id))
                        .filter(Optional::isPresent)
                        .map(user -> user.get().getUsername())
                        .collect(Collectors.toList());
            }
            responseDTO.setCurrentContributors(currentContributorNames);

            List<String> pastContributorNames = new ArrayList<>();
            if (savedProject.getPastContributorIds() != null && !savedProject.getPastContributorIds().isEmpty()) {
                pastContributorNames = savedProject.getPastContributorIds().stream()
                        .map(id -> userRepository.findById(id))
                        .filter(Optional::isPresent)
                        .map(user -> user.get().getUsername())
                        .collect(Collectors.toList());
            }
            responseDTO.setPastContributors(pastContributorNames);
        }

        return responseDTO;
    }

    public ProjectResponseDTO getProject(String ownerId,String projectId) {
        ObjectId objectId = new ObjectId(projectId);
        Project project = projectRepository.findById(objectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        ProjectResponseDTO responseDTO = projectMapper.toResponseEntity(project);

        // Add owner and contributor details
        Optional<User> owner = userRepository.findById(project.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());

            // Add contributor details
            List<String> currentContributorNames = new ArrayList<>();
            if (project.getCurrentContributorIds() != null && !project.getCurrentContributorIds().isEmpty()) {
                currentContributorNames = project.getCurrentContributorIds().stream()
                        .map(id -> userRepository.findById(id))
                        .filter(Optional::isPresent)
                        .map(user -> user.get().getUsername())
                        .collect(Collectors.toList());
            }
            responseDTO.setCurrentContributors(currentContributorNames);

            List<String> pastContributorNames = new ArrayList<>();
            if (project.getPastContributorIds() != null && !project.getPastContributorIds().isEmpty()) {
                pastContributorNames = project.getPastContributorIds().stream()
                        .map(id -> userRepository.findById(id))
                        .filter(Optional::isPresent)
                        .map(user -> user.get().getUsername())
                        .collect(Collectors.toList());
            }
            responseDTO.setPastContributors(pastContributorNames);
        }

        return responseDTO;
    }

    public boolean deleteProject(String projectId) {
        ObjectId objectId = new ObjectId(projectId);
        if (projectRepository.existsById(objectId)) {
            projectRepository.deleteById(objectId);
            return true;
        }
        return false;
    }
}