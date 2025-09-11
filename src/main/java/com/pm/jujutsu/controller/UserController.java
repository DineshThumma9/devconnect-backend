package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.mappers.UserMappers;
import com.pm.jujutsu.service.AzureBlobService;
import com.pm.jujutsu.service.Neo4jService;
import com.pm.jujutsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    public UserService userService;

    @Autowired
    public UserMappers userMappers;

    @Autowired
    public AzureBlobService azureBlobService;


    @Autowired
    public Neo4jService neo4jService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String id) {
        return ResponseEntity.ok().body(userService.getUser(id));
    }

    @PostMapping("/create-user")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO user) throws IllegalAccessException {
        UserResponseDTO newUser = userService.createUser(user);
        return ResponseEntity.ok(newUser);
    }

    @PutMapping("/update")
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody UserRequestDTO user) throws IllegalAccessException {
        return ResponseEntity.ok().body(userService.updateUser(user));
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("file") MultipartFile file) throws IOException {
        String profileUrl = azureBlobService.uploadFile(file);
        UserResponseDTO updatedUser = userService.updateProfilePicture(file);
        return ResponseEntity.ok(profileUrl);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/interests")
    public ResponseEntity<Void> userIntersets(@RequestBody UserRequestDTO user, @RequestBody String userId) {
        userService.updateUser(user);
        neo4jService.syncUserTags(userId, user.interests);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/follow/{userId}")
    public ResponseEntity<Void> followUser(
            @PathVariable("userId") String userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return userService.addFollower(userId, userDetails.getUsername()) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    @PutMapping("/unfollow/{userId}")
    public ResponseEntity<Void> unFollowUser(
            @PathVariable("userId") String userId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return userService.removeFollower(userId, userDetails.getUsername()) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    @GetMapping("/{userId}/suggested-connections")
    public ResponseEntity<List<UserResponseDTO>> suggestedConnection(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getRecommendConnections(userDetails.getUsername()));

    }


}
