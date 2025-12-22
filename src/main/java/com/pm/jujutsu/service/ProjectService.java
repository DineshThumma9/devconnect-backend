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
import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    


    @Autowired
    private SupabaseStorageService supabaseStorageService;

    private static final String CACHE_PROJECT = "projects";
    

    @CachePut(cacheNames = CACHE_PROJECT, key = "#result.projectId")
    public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO, List<MultipartFile> images) throws IOException {
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();

        Project project = projectMapper.toEntity(projectRequestDTO);
        project.setOwnerId(currentUserId);

        
        if (images != null && !images.isEmpty()) {
            List<String> mediaUrls = supabaseStorageService.uploadMultipleFiles(images, "projects");
            project.setMedia(mediaUrls.toArray(new String[0]));
        }
        
        Project savedProject = projectRepository.save(project);
        
    
        neo4jService.createProjectNode(
            savedProject.getId().toHexString(),
            savedProject.getTitle(),
            savedProject.getDescription()
        );
        
    

        neo4jService.createProjectOwnerRelationship(
            savedProject.getId().toHexString(),
            savedProject.getOwnerId().toHexString()
        );
        

        neo4jService.syncProjectTags(savedProject.getId().toHexString(), savedProject.getTechRequirements());
        
        return enrichProjectResponse(savedProject);
    }


    @CachePut(cacheNames = CACHE_PROJECT, key = "#result.projectId")
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

    @Cacheable(cacheNames = CACHE_PROJECT, key = "#projectId")
    public ProjectResponseDTO getProject(String projectId) {
        ObjectId objectId = new ObjectId(projectId);
        Project project = projectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        return enrichProjectResponse(project);
    }


    @CacheEvict(cacheNames = CACHE_PROJECT, key = "#projectId")
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



    @CacheEvict(cacheNames = CACHE_PROJECT, allEntries = true)
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


    @CacheEvict(cacheNames = CACHE_PROJECT, allEntries = true)
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


    @Cacheable(cacheNames = CACHE_PROJECT, key = "'trendingProjects'")
    public List<ProjectResponseDTO> getTrendingProjects(){
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();
        List<Project> projects = projectRepository.findAllByCurrentContributorIds(currentUserId);
        return projects.stream().map(projectMapper::toResponseEntity).toList();


    }



    @Cacheable(cacheNames = CACHE_PROJECT, key = "'recommend#' + #username")
    public List<ProjectResponseDTO> recommendProjects(String username){

        Optional<User> userOpt = userRepository.findByUsername(username);

        if(userOpt.isEmpty()){
            return List.of();
        }

        User user = userOpt.get();
        String userId = user.getId().toHexString();
        
        List<String> recommendPosts = neo4jService.getProjectBasedOnInterests(userId,user.getInterests());
        List<String> recommendPostFromConnections = neo4jService.recommendProjectBasedOnConnectionsAndTags(userId, user.getInterests());

        
        recommendPosts.addAll(recommendPostFromConnections);
        List<String> uniqueUserIds = recommendPosts.stream().distinct().toList();


        List<ObjectId> objectIds = uniqueUserIds.stream()
                .map(ObjectId::new)
                .toList();

    
        List<Project> projects = StreamSupport.stream(projectRepository.findAllById(objectIds).spliterator(), false)
                .collect(Collectors.toList());

    
        return projects.stream()
                .map(projectMapper::toResponseEntity)
                .collect(Collectors.toList());


    }



    


    @Cacheable(cacheNames = CACHE_PROJECT, key = "'allProjects#' + #username")
    public List<ProjectResponseDTO> getAllProjects(String username){


        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        ObjectId userId = user.getId();
        List<Project> projects = projectRepository.findAll();
        return projects
            .stream()
            .filter(project -> project.getOwnerId().equals(userId))
            .map(projectMapper::toResponseEntity)
            .toList();
    }

}
