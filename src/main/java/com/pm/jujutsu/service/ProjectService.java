package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.ProjectRequestDTO;
import com.pm.jujutsu.dtos.ProjectResponseDTO;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.exceptions.UnauthorizedException;
import com.pm.jujutsu.mappers.ProjectMapper;
import com.pm.jujutsu.model.*;
import com.pm.jujutsu.repository.ProjectNodeRepository;
import com.pm.jujutsu.repository.ProjectRepository;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.JwtUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Set;
@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProjectNodeRepository projectNodeRepository;

    @Autowired
    private Neo4jService neo4jService;



    public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO) {
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();

        Project project = projectMapper.toEntity(projectRequestDTO);
        project.setOwnerId(currentUserId);
        Project savedProject = projectRepository.save(project);
        
        // Create and save ProjectNode in Neo4j
        ProjectNode projectNode = new ProjectNode();
        projectNode.setId(savedProject.getId().toHexString());
        projectNode.setOwner(neo4jService.getUserById(savedProject.getOwnerId().toHexString()).get());
        projectNodeRepository.save(projectNode);
        neo4jService.syncProjectTags(savedProject.getId().toHexString(), savedProject.getTechRequirements());
        return enrichProjectResponse(savedProject);
    }

    public ProjectResponseDTO updateProject(String projectId, ProjectRequestDTO projectRequestDTO) {
        ObjectId objectId = new ObjectId(projectId);
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();

        Project project = projectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (!project.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedException("Not authorized to update this project");
        }

        project.setTitle(projectRequestDTO.getTitle());
        project.setDescription(projectRequestDTO.getDescription());
        project.setTechRequirements(projectRequestDTO.getTechRequirements());
        project.setPrivate(projectRequestDTO.isPrivate());
        project.setGithubLink(projectRequestDTO.getGithubLink());

        Project savedProject = projectRepository.save(project);
        return enrichProjectResponse(savedProject);
    }

    public ProjectResponseDTO getProject(String projectId) {
        ObjectId objectId = new ObjectId(projectId);
        Project project = projectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        return enrichProjectResponse(project);
    }

    public boolean deleteProject(String projectId) {
        ObjectId objectId = new ObjectId(projectId);
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();

        Project project = projectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        if (!project.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedException("Not authorized to delete this project");
        }

        projectRepository.deleteById(objectId);
        return true;
    }




    private ProjectResponseDTO enrichProjectResponse(Project project) {
        ProjectResponseDTO responseDTO = projectMapper.toResponseEntity(project);

        // Add owner details
        Optional<User> owner = userRepository.findById(project.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());

            // Add current contributors
            if (project.getCurrentContributorIds() != null && !project.getCurrentContributorIds().isEmpty()) {
                Set<String> currentContributorNames = project.getCurrentContributorIds().stream()
                        .filter(id -> id != null)
                        .map(id -> userRepository.findById(id))
                        .filter(Optional::isPresent)
                        .map(user -> user.get().getUsername())
                        .collect(Collectors.toSet());
                responseDTO.setCurrentContributors(currentContributorNames);
            }

        }

        return responseDTO;
    }



    public boolean subscribeToProject(String projectId,String userId){
        ObjectId objectProjectId = new ObjectId(projectId);
        ObjectId objectUserId = new ObjectId(userId);
        if(objectProjectId == null || objectUserId == null){
            throw new NotFoundException("Project or User ID is Empty");
        }
        Optional<User> user = userRepository.findById(objectUserId);
        if(user.isEmpty()){
            return false;

        }
        List<Project> projectList = projectRepository.findAllById(user.get().getSubscribedProjectIds());
        Optional<Project> project = projectRepository.findById(objectProjectId);
        if(project.isEmpty()){
            throw new NotFoundException("Project is Empty");
        }
        projectList.add(project.get());
        neo4jService.subscribeToProject(userId,projectId);
        return true;



    }


    public boolean unsubscribeToProject(String projectId,String userId){
        ObjectId objectProjectId = new ObjectId(projectId);
        ObjectId objectUserId = new ObjectId(userId);
        if(objectProjectId == null || objectUserId == null){
            throw new NotFoundException("Project or User ID is Empty");
        }
        Optional<User> user = userRepository.findById(objectUserId);
        if(user.isEmpty()){
            return false;

        }
        List<Project> projectList = projectRepository.findAllById(user.get().getSubscribedProjectIds());
        Optional<Project> project = projectRepository.findById(objectProjectId);
        if(project.isEmpty()){
            throw new NotFoundException("Project is Empty");
        }
        projectList.remove(project.get());
        neo4jService.unsubscribeFromProject(userId,projectId);
        return true;



    }





    // public List<ProjectResponseDTO> searchForProject(
    //         String projectTitle,
    //         String projectDesc,
    //         List<String> tags,
    //         String userId
    // ){

    // // TODO




    // }


    public List<ProjectResponseDTO> getTrendingProjects(){
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();
        List<Project> projects = projectRepository.findAllByCurrentContributorIds(currentUserId);
        return projects.stream().map(projectMapper::toResponseEntity).toList();


    }



    public List<ProjectResponseDTO> recommendProjects(String userId){

        ObjectId objectId = new ObjectId(userId);
        Optional<User> userOpt = userRepository.findById(objectId);

        if(userOpt.isEmpty()){
            return List.of();
        }

        User user = userOpt.get();
        List<String> recommendPosts = neo4jService.getProjectBasedOnInterests(userId,user.getInterests());
        List<String> recommendPostFromConnections = neo4jService.recommendProjectBasedOnConnectionsAndTags(userId, user.getInterests());

        // Combine and remove duplicates
        recommendPosts.addAll(recommendPostFromConnections);
        List<String> uniqueUserIds = recommendPosts.stream().distinct().toList();


        List<ObjectId> objectIds = uniqueUserIds.stream()
                .map(ObjectId::new)
                .toList();

        // Fetch users from repository
        List<Project> projects = StreamSupport.stream(projectRepository.findAllById(objectIds).spliterator(), false)
                .collect(Collectors.toList());

        // Map to DTOs
        return projects.stream()
                .map(projectMapper::toResponseEntity)
                .collect(Collectors.toList());


    }

}
