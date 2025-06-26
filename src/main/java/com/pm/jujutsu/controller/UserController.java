package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.mappers.UserMappers;
import com.pm.jujutsu.service.AzureBlobService;
import com.pm.jujutsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    public UserService userService;

    @Autowired
    public UserMappers userMappers;

    @Autowired
    public AzureBlobService azureBlobService;

    @GetMapping("/get-user/{id}")
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
