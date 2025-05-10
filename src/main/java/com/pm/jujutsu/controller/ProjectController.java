package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.ProjectRequestDTO;
import com.pm.jujutsu.dtos.ProjectResponseDTO;
import com.pm.jujutsu.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/get-project/{ownerId}/{projectId}")
    public ResponseEntity<ProjectResponseDTO> getProject(@PathVariable String ownerId,@PathVariable String projectId) {
        ProjectResponseDTO projectResponseDTO = projectService.getProject(ownerId,projectId);
        return ResponseEntity.ok(projectResponseDTO);
    }

    @PostMapping("/create/{ownerId}")
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestParam("ownerId") String ownerId,
                                                      @RequestBody ProjectRequestDTO projectRequestDTO) {
        ProjectResponseDTO projectResponseDTO = projectService.createProject(ownerId,projectRequestDTO);
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
}