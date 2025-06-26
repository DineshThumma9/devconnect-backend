package com.pm.jujutsu.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;



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


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @Email @NotNull String getEmail() {
        return email;
    }

    public void setEmail(@Email @NotNull String email) {
        this.email = email;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public void setUsername(@NotNull String username) {
        this.username = username;
    }

    public @NotNull String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(@NotNull String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public @NotNull String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(@NotNull String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
