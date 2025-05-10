package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.mappers.UserMappers;
import com.pm.jujutsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    public UserService userService;

    @Autowired
    public UserMappers userMappers;

    @GetMapping("/get-user/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@RequestParam String id) {
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

    @DeleteMapping("/delete/")
    public ResponseEntity<Void> deleteUser(@Param("id") String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}