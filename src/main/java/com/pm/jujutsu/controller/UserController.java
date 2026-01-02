package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.dtos.UserUpdateDTO;
import com.pm.jujutsu.mappers.UserMappers;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody UserUpdateDTO user) throws IllegalAccessException {
        return ResponseEntity.ok().body(userService.updateUser(user));
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<UserResponseDTO> uploadProfilePicture(@RequestParam("file") MultipartFile file) throws IOException {
        UserResponseDTO updatedUser = userService.updateProfilePicture(file);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
       return  userService.deleteUser(username) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }



    @GetMapping("/following/{username}")
    public ResponseEntity<List<UserResponseDTO>>  userFollowings(@PathVariable String username) {
         List<UserResponseDTO> followings = userService.getFollowings(username);
         return ResponseEntity.ok(followings);
        
    }


    @GetMapping("followers/{username}")
    public ResponseEntity<List<UserResponseDTO>>  userFollowers(@PathVariable String username) {
        return ResponseEntity.ok(userService.getFollowers(username));
    }
    

    @GetMapping("/chats")
    public String userChats(@RequestParam String param) {
        return new String();
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




    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<UserResponseDTO> results = userService.getUsersByName(q.trim(), page, size);
        return ResponseEntity.ok(results);
    }
    
    // Debug endpoint to see all users
    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        System.out.println("Total users in DB: " + users.size());
        users.forEach(user -> {
            System.out.println("  - Name: " + user.getName() + ", Username: " + user.getUsername());
        });
        return ResponseEntity.ok(users);
    }
    
    // @GetMapping("/{username}/suggested-connections")
    // public ResponseEntity<List<UserResponseDTO>> suggestedConnection(
    //         @AuthenticationPrincipal UserDetails userDetails
    // ) {
    //     return ResponseEntity.ok(userService.getRecommendConnections(userDetails.getUsername()));

    // }


}
