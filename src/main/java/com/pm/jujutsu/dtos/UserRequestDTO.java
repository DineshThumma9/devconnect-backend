package com.pm.jujutsu.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserRequestDTO {





   /*
   Should frontend send uuid of used and then we save it or backend should
   generate it what is best here
   
   */
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