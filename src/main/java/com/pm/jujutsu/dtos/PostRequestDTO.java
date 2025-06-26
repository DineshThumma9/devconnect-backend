package com.pm.jujutsu.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.internal.util.StringHelper;

@Data
@Getter
@Setter
public class PostRequestDTO {

    @NotNull
    private String title;

     @NotNull
    private String content;


     private String[] media;


    public @NotNull String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public @NotNull String getContent() {
        return content;
    }

    public void setContent(@NotNull String content) {
        this.content = content;
    }
}
