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

    @PostMapping("/create/")
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



    @GetMapping("/trending-projects")
    public ResponseEntity<PostResponseDTO> getTrendingProjects(
     @AuthenticationPrincipal User user
    ){



    }


    @GetMapping("/for-you-projects")
    public ResponseEntity<PostResponseDTO> forYouProjects(
            @AuthenticationPrincipal User user
    ){
        ObjectId id = user.getId();
        User user = userService.getUser(id.toHexString());


        List<ProjectResponseDTO> forYouProjects = neo4jService.getProjectBasedOnConnectionsAndInterests(user.getId(),user.getInterests());


        return forYouProjects;
    }
}