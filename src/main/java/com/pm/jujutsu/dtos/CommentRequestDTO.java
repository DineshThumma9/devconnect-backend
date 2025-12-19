package com.pm.jujutsu.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDTO {

    @NotNull
    @NotBlank(message = "Comment text cannot be blank")
    private String comment;

    @NotNull
    @NotBlank(message = "Post ID cannot be blank")
    private String postId;
}
