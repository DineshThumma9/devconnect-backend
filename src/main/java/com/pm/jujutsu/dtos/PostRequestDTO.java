package com.pm.jujutsu.dtos;



import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.util.HashSet;
import java.util.Set;

@Data
public class PostRequestDTO {

    @NotNull
    private String title;

    @NotNull
    private String content;

    private Set<String> tags = new HashSet<>();

    private String[] media;


}
