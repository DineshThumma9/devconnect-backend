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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/projects")
public class ProjectController {


    @Autowired
    private ProjectService projectService;

    

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProject(@PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }



    @PostMapping("/create")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestPart("project") ProjectRequestDTO projectRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        return ResponseEntity.ok(projectService.createProject(projectRequestDTO, images));
    }
    
    @PostMapping("/create-json")
    public ResponseEntity<ProjectResponseDTO> createProjectJson(
            @Valid @RequestBody ProjectRequestDTO projectRequestDTO
    ) throws IOException {
        return ResponseEntity.ok(projectService.createProject(projectRequestDTO, null));
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

    @GetMapping("/subscribed/{username}")
    public List<ProjectResponseDTO> getMethodName(@PathVariable String username) {
        return projectService.getUserInvolvedProjects(username);
    }

    @GetMapping("/all/{username}")
    public List<ProjectResponseDTO> getAllProjects(
            @PathVariable String username
    ) {
        List<ProjectResponseDTO> projects =
        projectService.getAllProjects(username);
        projects.addAll(projectService.getUserInvolvedProjects(username));
        return projects;
        
    }    

   @GetMapping("/search")
    public ResponseEntity<List<ProjectResponseDTO>> searchProjects(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<ProjectResponseDTO> results = projectService.searchProjects(q.trim(), page, size);
        return ResponseEntity.ok(results);
    }
    
    // @GetMapping("/get-projects/{username}")
    // public List<ProjectResponseDTO>  getAllProjectsOfUser(@PathVariable String username) {
    //     return projectService.getAllProjects(username);
    // }
    
}
