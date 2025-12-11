package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.*;
import com.pm.jujutsu.model.*;
import com.pm.jujutsu.utils.FakeDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dev")
public class DevDataController {

    @Autowired
    private FakeDataGenerator fakeDataGenerator;

    @GetMapping("/fake-user")
    public ResponseEntity<Map<String, Object>> generateFakeUser() {
        Map<String, Object> response = new HashMap<>();
        
        User user = fakeDataGenerator.generateFakeUser();
        UserRequestDTO userRequestDTO = fakeDataGenerator.generateFakeUserRequestDTO();
        
        response.put("userModel", user);
        response.put("userRequestDTO", userRequestDTO);
        response.put("description", "Generated fake user data - Model vs DTO");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-users/{count}")
    public ResponseEntity<Map<String, Object>> generateFakeUsers(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        
        List<User> users = fakeDataGenerator.generateFakeUsers(count);
        
        response.put("users", users);
        response.put("count", users.size());
        response.put("description", "Generated " + count + " fake users");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-post")
    public ResponseEntity<Map<String, Object>> generateFakePost() {
        Map<String, Object> response = new HashMap<>();
        
        Post post = fakeDataGenerator.generateFakePost();
        PostRequestDTO postRequestDTO = fakeDataGenerator.generateFakePostRequestDTO();
        
        response.put("postModel", post);
        response.put("postRequestDTO", postRequestDTO);
        response.put("description", "Generated fake post data - Model vs DTO");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-posts/{count}")
    public ResponseEntity<Map<String, Object>> generateFakePosts(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        
        List<Post> posts = fakeDataGenerator.generateFakePosts(count);
        
        response.put("posts", posts);
        response.put("count", posts.size());
        response.put("description", "Generated " + count + " fake posts");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-project")
    public ResponseEntity<Map<String, Object>> generateFakeProject() {
        Map<String, Object> response = new HashMap<>();
        
        Project project = fakeDataGenerator.generateFakeProject();
        ProjectRequestDTO projectRequestDTO = fakeDataGenerator.generateFakeProjectRequestDTO();
        
        response.put("projectModel", project);
        response.put("projectRequestDTO", projectRequestDTO);
        response.put("description", "Generated fake project data - Model vs DTO");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-projects/{count}")
    public ResponseEntity<Map<String, Object>> generateFakeProjects(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        
        List<Project> projects = fakeDataGenerator.generateFakeProjects(count);
        
        response.put("projects", projects);
        response.put("count", projects.size());
        response.put("description", "Generated " + count + " fake projects");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-comment")
    public ResponseEntity<Map<String, Object>> generateFakeComment() {
        Map<String, Object> response = new HashMap<>();
        
        Comment comment = fakeDataGenerator.generateFakeComment();
        
        response.put("commentModel", comment);
        response.put("description", "Generated fake comment data");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-all")
    public ResponseEntity<Map<String, Object>> generateAllFakeData() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("users", fakeDataGenerator.generateFakeUsers(3));
        response.put("posts", fakeDataGenerator.generateFakePosts(5));
        response.put("projects", fakeDataGenerator.generateFakeProjects(3));
        response.put("comments", List.of(
                fakeDataGenerator.generateFakeComment(),
                fakeDataGenerator.generateFakeComment(),
                fakeDataGenerator.generateFakeComment()
        ));
        
        response.put("description", "Complete set of fake data for all models");
        
        return ResponseEntity.ok(response);
    }
}
