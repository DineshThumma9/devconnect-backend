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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    

    @CachePut(cacheNames = CACHE_PROJECT, key = "#result.id")
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


    @CachePut(cacheNames = CACHE_PROJECT, key = "#result.id")
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
            System.out.println("❌ User not found for recommendations: " + username);
            return List.of();
        }

        User user = userOpt.get();
        String userId = user.getId().toHexString();
        
        System.out.println("🔍 Getting project recommendations for user: " + username);
        System.out.println("   User ID: " + userId);
        System.out.println("   Interests: " + user.getInterests());
        
        if (user.getInterests() == null || user.getInterests().isEmpty()) {
            System.out.println("⚠️ User has no interests - cannot recommend projects");
            return List.of();
        }
        
        List<String> recommendPosts = neo4jService.getProjectBasedOnInterests(userId,user.getInterests());
        List<String> recommendPostFromConnections = neo4jService.recommendProjectBasedOnConnectionsAndTags(userId, user.getInterests());

        System.out.println("📊 Projects based on interests: " + recommendPosts.size());
        System.out.println("📊 Projects based on follows: " + recommendPostFromConnections.size());
        
        recommendPosts.addAll(recommendPostFromConnections);
        List<String> uniqueUserIds = recommendPosts.stream().distinct().toList();
        
        System.out.println("📊 Total unique project recommendations: " + uniqueUserIds.size());


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

     public List<ProjectResponseDTO> getUserInvolvedProjects(String username){

         Optional<User> user = userRepository.findByUsername(username);
         if(user.isEmpty()){
            throw new UsernameNotFoundException("Username not found");
         }

         ObjectId objectId = user.get().getId();
         List<Project> projects = projectRepository.findAllByCurrentContributorIdsContains(objectId);
         return projects.stream()
                 .map(projectMapper::toResponseEntity)
                 .toList();


     }


    public List<ProjectResponseDTO> searchProjects(String query, int page, int size) {
        System.out.println("\n========== PROJECT SEARCH STARTED ==========");
        System.out.println("Search query: '" + query + "' (page: " + page + ", size: " + size + ")");
        
        // Create pageable with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "_id"));
        
        List<Project> projects = projectRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query, pageable);
        System.out.println("Found " + projects.size() + " projects (page " + page + ")");
        
        if (!projects.isEmpty()) {
            System.out.println("Project results:");
            projects.forEach(project -> {
                System.out.println("  - ID: " + project.getId() + ", Title: " + project.getTitle());
                System.out.println("    Description: " + (project.getDescription().length() > 60 ? 
                    project.getDescription().substring(0, 60) + "..." : project.getDescription()));
                System.out.println("    Tech: " + project.getTechRequirements());
            });
        } else {
            System.out.println("No projects found matching: '" + query + "'");
        }
        
        List<ProjectResponseDTO> result = projects.stream()
                .map(this::enrichProjectResponse)
                .collect(Collectors.toList());
        
        System.out.println("========== PROJECT SEARCH COMPLETED ==========\n");
        return result;
    }

}