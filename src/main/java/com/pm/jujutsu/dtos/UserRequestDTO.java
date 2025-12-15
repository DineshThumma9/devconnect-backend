package com.pm.jujutsu.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserRequestDTO {
    // For creating new users (registration) - all required

    @NotBlank(message = "Name is required")
    public String name;

    @NotBlank(message = "Password is required")
    public String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    public String email;

    @NotBlank(message = "Username is required")
    public String username;

    public String profilePicUrl;

    public Set<String> interests = new HashSet<>();
}