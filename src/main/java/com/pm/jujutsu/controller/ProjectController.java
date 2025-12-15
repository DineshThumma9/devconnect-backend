package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.ProjectRequestDTO;
import com.pm.jujutsu.dtos.ProjectResponseDTO;
import com.pm.jujutsu.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;



@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProject(@PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }



    /*
    Should i make PostMapping with a query mapping with projectID frontend will randomly generate
    UUID that backend will save 
     */



    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestPart("project") ProjectRequestDTO projectRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    )  {
        return ResponseEntity.ok(projectService.createProject(projectRequestDTO, images));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable String projectId,
            @Valid @RequestBody ProjectRequestDTO projectRequestDTO) {
        return ResponseEntity.ok(projectService.updateProject(projectId, projectRequestDTO));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        return projectService.deleteProject(projectId)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{projectId}/subscribe")
    public ResponseEntity<Void> subscribe(
            @PathVariable String projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return projectService.subscribeToProject(projectId, userDetails.getUsername())
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{projectId}/subscribe")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable String projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return projectService.unsubscribeToProject(projectId, userDetails.getUsername())
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

   
    @GetMapping("/get-projects/{username}")
    public List<ProjectResponseDTO> getAllProjectsOfUser(@PathVariable String username) {
        return projectService.getAllProjects(username);
    }

    // @GetMapping("/get-projects/{username}")
    // public List<ProjectResponseDTO>  getAllProjectsOfUser(@PathVariable String username) {
    //     return projectService.getAllProjects(username);
    // }
    
}
