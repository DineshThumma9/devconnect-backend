package com.pm.jujutsu.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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

    public String profile_pic;


    public List<String> interests = new ArrayList<>();



}