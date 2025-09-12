package com.pm.jujutsu.model;




import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

    private Set<ObjectId> followerIds = new HashSet<>();
    private Set<ObjectId> followingIds = new HashSet<>();
    private Set<ObjectId> subscribedProjectIds = new HashSet<>();


    private Set<String> interests = new HashSet<>();

    public void addInterest(String interest) {
        interests.add(interest);
    }

    public void removeInterest(String interest) {
        interests.remove(interest);
    }

    
}
