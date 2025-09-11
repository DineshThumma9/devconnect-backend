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

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO projectRequestDTO) {
        return ResponseEntity.ok(projectService.createProject(projectRequestDTO));
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

    @GetMapping("/trending")
    public ResponseEntity<List<ProjectResponseDTO>> getTrendingProjects() {
        return ResponseEntity.ok(projectService.getTrendingProjects());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProjectResponseDTO>> recommendProjects(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.recommendProjects(userDetails.getUsername()));
    }
}
