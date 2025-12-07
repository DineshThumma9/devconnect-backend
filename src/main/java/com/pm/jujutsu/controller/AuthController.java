package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.LoginRequestDTO;
import com.pm.jujutsu.dtos.LoginResponseDTO;
import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.service.AuthService;
import com.pm.jujutsu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.logging.Logger;

@RequestMapping("/auth")
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;



    Logger logger  = Logger.getLogger("auth");

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO requestBody) {
        logger.info("IN Login endpoint");
        return authService.login(requestBody)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }




    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@RequestBody UserRequestDTO userRequestDTO) {

        logger.info("In Register Endpoint");
        UserResponseDTO user = userService.createUser(userRequestDTO);
        System.out.println("User :" + user);
        if (user != null) {
             ResponseEntity<LoginResponseDTO> entity = authService.login(new LoginRequestDTO(userRequestDTO.getEmail(), userRequestDTO.getPassword()))
                    .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());


            logger.info("Sending Entity" + entity );
            return entity;
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping(value = "/register-with-profile-pic", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LoginResponseDTO> registerWithProfilePic(
            @RequestPart("user") UserRequestDTO userRequestDTO,
            @RequestPart(value = "profilePic", required = false) MultipartFile profilePic
    ) throws IOException {

//        if (profilePic != null && !profilePic.isEmpty()) {
//            String profilePicUrl = azureBlobService.uploadFile(profilePic);
//            userRequestDTO.setProfilePicUrl(profilePicUrl);
//        }

        UserResponseDTO user = userService.createUser(userRequestDTO);

        if (user != null) {
            return authService.login(new LoginRequestDTO(userRequestDTO.getEmail(), userRequestDTO.getPassword()))
                    .map(token -> ResponseEntity.status(HttpStatus.CREATED).body(token))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (authService.validateToken(token)) {
                return ResponseEntity.ok("Token is valid");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
    }

}
