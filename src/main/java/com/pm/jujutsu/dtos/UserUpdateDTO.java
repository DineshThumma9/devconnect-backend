package com.pm.jujutsu.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class UserUpdateDTO {
    // For updating user profile - all fields optional except email (to identify user)
    public String email;  // Required to find user
    public String name;
    public String username;
    public String profilePicUrl;
    public Set<String> interests;
}
