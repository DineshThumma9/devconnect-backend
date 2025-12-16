package com.pm.jujutsu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.dtos.ProjectResponseDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.service.PostService;
import com.pm.jujutsu.service.UserService;
import com.pm.jujutsu.service.ProjectService;

@RequestMapping("/feed")
@RestController
public class FeedController {
    

    

    @Autowired
    public UserService userService;

    @Autowired
    public PostService postService;

    @Autowired
    public ProjectService projectService;
    
    @GetMapping("/suggested-connections/{username}")
    public ResponseEntity<List<UserResponseDTO>> suggestedConnection(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getRecommendConnections(userDetails.getUsername()));

    }


     

    
    @GetMapping("/trending/posts")
    public ResponseEntity<List<PostResponseDTO>> getTrendingPost(

    ) {

        return ResponseEntity.ok(postService.getTrendingPost());

    }


    @GetMapping("/for-you/posts")
    public ResponseEntity<List<PostResponseDTO>> forYouPosts(

    ) {

        return ResponseEntity.ok(postService.getRecommendPosts());

    }
     

        @GetMapping("/recommendations")
    public ResponseEntity<List<ProjectResponseDTO>> recommendProjects(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(projectService.recommendProjects(userDetails.getUsername()));
    }


      @GetMapping("/trending/projects")
    public ResponseEntity<List<ProjectResponseDTO>> getTrendingProjects() {
        return ResponseEntity.ok(projectService.getTrendingProjects());
    }


}
