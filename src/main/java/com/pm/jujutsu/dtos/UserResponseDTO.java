package com.pm.jujutsu.dtos;


import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class UserResponseDTO {


    public  String id;
    public String username;
    public  String email;
    public String profilePicUrl;
    public Set<UserResponseDTO> followers = new HashSet<>();
    public Set<UserResponseDTO> followings = new HashSet<>();
    public Set<ProjectResponseDTO> subscribedProjects = new HashSet<>();


}
