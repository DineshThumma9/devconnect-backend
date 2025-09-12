package com.pm.jujutsu.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.internal.util.StringHelper;

import java.util.HashSet;
import java.util.Set;

@Data
@Getter
@Setter
public class PostRequestDTO {

    @NotNull
    private String title;

     @NotNull
    private String content;

    private Set<String> tags = new HashSet<>();

     private String[] media;


}
