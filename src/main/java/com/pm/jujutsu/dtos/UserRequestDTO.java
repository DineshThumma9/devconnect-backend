package com.pm.jujutsu.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class UserRequestDTO {

    @NotBlank
    public String name;

    @NotBlank
    public String password;

    @NotBlank
    public String email;

    @NotBlank
    public String username;

    public String profilePicUrl;


    public Set<String> interests = new HashSet<>();



}