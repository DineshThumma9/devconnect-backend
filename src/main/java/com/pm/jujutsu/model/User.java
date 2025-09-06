package com.pm.jujutsu.model;


import com.pm.jujutsu.dtos.UserResponseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;


@Document(value = "users")
@Data
public class User {



    @Id
    private ObjectId id;

    @NotNull
    private String name;

    @Email
    @NotNull
    @Indexed(unique = true)
    private String email;

    @NotNull
    @Indexed(unique = true)
    private String username;

    @NotNull
    private String hashedPassword;

    @NotNull
    private String profilePicUrl;


    private List<UserResponseDTO> followers = new ArrayList<>();
    private List<UserResponseDTO> following = new ArrayList<>();
    private List<String> interests = new ArrayList<>();


    public void removeFollower(User user) {
    }

    public void setFollower(User user) {
    }
}
