package com.pm.jujutsu.model;




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



    private List<User> followers = new ArrayList<>();

    private List<User> following = new ArrayList<>();


    private List<String> interests = new ArrayList<>();

    public List<Project> subscribedProjects = new ArrayList<>();



    public void removeFollower(User user) {
        followers.remove(user);
    }

    public void setFollower(User user) {
        followers.add(user);
    }
}
