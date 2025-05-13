package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.LoginRequestDTO;
import com.pm.jujutsu.dtos.LoginResponseDTO;
import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.service.AuthService;
import com.pm.jujutsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/auth")
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO requestBody
    ) {
        Optional<String> token = authService.login(requestBody);
        if (token.isPresent()) {
            return ResponseEntity.ok(new LoginResponseDTO(token.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(
            @RequestBody UserRequestDTO userRequestDTO

    ) {


        UserResponseDTO user = null;

            user = userService.createUser(userRequestDTO);

        if (user != null) {
            String token = authService.login(new LoginRequestDTO(userRequestDTO.getEmail(), userRequestDTO.getPassword()))
                    .orElse(null);
            if (token != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponseDTO(token));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (authService.validateToken(token)) {
                return ResponseEntity.ok("Token is valid");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
    }
}
