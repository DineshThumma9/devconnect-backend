package com.pm.jujutsu.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDTO {

    @NotNull
    @NotBlank
    private String comment;

    @NotNull
    private String postId;
}
