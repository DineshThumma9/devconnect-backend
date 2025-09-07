package com.pm.jujutsu.dtos;


import lombok.Data;

import java.util.List;

@Data
public class UserResponseDTO {


    public  String id;
    public String username;
    public  String email;
    public String profilePicUrl;
    public List<UserResponseDTO> followers;
    public List<UserResponseDTO> followings;
    public List<ProjectResponseDTO> subscribedProjects;


}
