package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.dtos.ProjectRequestDTO;
import com.pm.jujutsu.dtos.ProjectResponseDTO;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.service.Neo4jService;
import com.pm.jujutsu.service.ProjectService;
import com.pm.jujutsu.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private Neo4jService neo4jService;
    @Autowired
    private UserService userService;

    @GetMapping("/get-project/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProject(@PathVariable String projectId) {
        ProjectResponseDTO projectResponseDTO = projectService.getProject(projectId);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> createProject(
                                                      @RequestBody ProjectRequestDTO projectRequestDTO) {
        ProjectResponseDTO projectResponseDTO = projectService.createProject(projectRequestDTO);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @PutMapping("/update/{projectId}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable String projectId,
            @RequestBody ProjectRequestDTO projectRequestDTO) {
        ProjectResponseDTO projectResponseDTO = projectService.updateProject(projectId, projectRequestDTO);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        if (projectService.deleteProject(projectId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }


    @PutMapping("/subscribe/{userId}/{projectId}")
    public ResponseEntity<Void> subscribeTo(
            @PathVariable("projectId") String projectId,
            @PathVariable("userId") String userId
    ){
        return projectService.subscribeToProject(projectId,userId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

    }


    @DeleteMapping("/subscribe/{userId}/{projectId}")
    public ResponseEntity<Void> unsubscribeTo(
            @PathVariable("projectId") String projectId,
            @PathVariable("userId") String userId
    ){
        return projectService.unsubscribeToProject(projectId,userId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

    }



    @GetMapping("/trending-projects")
    public ResponseEntity<List<ProjectResponseDTO>> getTrendingProjects(
     @AuthenticationPrincipal User user
    ){


        return ResponseEntity.ok(projectService.getTrendingProjects());


    }


    @GetMapping("/for-you-projects/{userId}")
    public ResponseEntity<List<ProjectResponseDTO>> forYouProjects(
            @PathVariable("userId") String userId
    ){

        return ResponseEntity.ok(projectService.recommendProjects(userId));
    }
}