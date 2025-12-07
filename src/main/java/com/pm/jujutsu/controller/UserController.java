package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.mappers.UserMappers;
import com.pm.jujutsu.service.Neo4jService;
import com.pm.jujutsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    public UserService userService;

    @Autowired
    public UserMappers userMappers;


    @Autowired
    public Neo4jService neo4jService;

    @GetMapping("/{username}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String username) {
        return ResponseEntity.ok().body(userService.getUser(username));
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

//    @PostMapping("/upload-profile-picture")
//    public ResponseEntity<UserResponseDTO> uploadProfilePicture(@RequestParam("file") MultipartFile file) throws IOException {
//        UserResponseDTO updatedUser = userService.updateProfilePicture(file);
//        return ResponseEntity.ok(updatedUser);
//    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/interests")
    public ResponseEntity<Void> userInterests(@RequestBody UserRequestDTO user) {
        UserResponseDTO updatedUser = userService.updateUser(user);
        neo4jService.syncUserTags(updatedUser.getId(), user.interests);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/follow/{username}")
    public ResponseEntity<Void> followUser(
            @PathVariable("username") String username,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return userService.addFollower(username, userDetails.getUsername()) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    @PutMapping("/unfollow/{username}")
    public ResponseEntity<Void> unFollowUser(
            @PathVariable("username") String username,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return userService.removeFollower(username, userDetails.getUsername()) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    @GetMapping("/{username}/suggested-connections")
    public ResponseEntity<List<UserResponseDTO>> suggestedConnection(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.getRecommendConnections(userDetails.getUsername()));

    }


}
