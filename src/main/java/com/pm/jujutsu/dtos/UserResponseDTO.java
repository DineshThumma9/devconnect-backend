package com.pm.jujutsu.dtos;


import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserResponseDTO {

    public String id;
    public String name;
    public String username;
    public String email;
    public String profilePicUrl;
    public Set<String> interests = new HashSet<>();
    
    /*
    Here we have UserResponseDTO but if i set 
    followers and following to as UserResponseDTO will it be and infinate recurrsion
    also should i use String or and ObjectId and
    if do that how can i show username and email 
    profilePic on frontend if i only pass String for followrs and follwing
    
     */
    public Set<String> followers = new HashSet<>();
    public Set<String> followings = new HashSet<>();
    public Set<String> subscribedProjects = new HashSet<>();

}
