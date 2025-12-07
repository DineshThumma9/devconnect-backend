package com.pm.jujutsu.controller;


import com.pm.jujutsu.dtos.CommentRequestDTO;
import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {


    @Autowired
    public PostService postService;


    @GetMapping("/{username}")
    public ResponseEntity<PostResponseDTO> getPost(
            @PathVariable String username
    ) {


        PostResponseDTO post = postService.getPost(username);
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();

    }


    @PostMapping("/create")
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody PostRequestDTO postRequestDTO) {



        return ResponseEntity.ok(postService.createPost(postRequestDTO));

    }


    @PutMapping("/{username}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable("username") String username,
            @RequestBody PostRequestDTO postRequestDTO

    ) {

        return ResponseEntity.ok(postService.updatePost(postRequestDTO, username));


    }


    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("username") String username
    ) {


        return postService.deletePost(username) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();


    }


    @PutMapping("/like/{postId}")
    public ResponseEntity<Void> likeAPost(
            @PathVariable("postId") String postId

    ) {

        return postService.increaseLike(postId) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();

    }

    @DeleteMapping("/like/{postId}")
    public ResponseEntity<Void> unlikePost(
            @PathVariable("postId") String postId


    ) {
        return postService.decreaseLike(postId) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();
    }

    @PutMapping("/comment/{postId}")
    public ResponseEntity<Void> commentOnPost(
            CommentRequestDTO comment


    ) {

        return postService.commentOnPost(comment.getPostId(), comment.getComment()) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();

    }

    @PutMapping("/share/{username}")
    public ResponseEntity<Void> share(
            @PathVariable("username") String username
    ) {

        return postService.shareAPost(username) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();

    }


    @GetMapping("/trending")
    public ResponseEntity<List<PostResponseDTO>> getTrendingPost(

    ) {

        return ResponseEntity.ok(postService.getTrendingPost());

    }


    @GetMapping("/for-you")
    public ResponseEntity<List<PostResponseDTO>> forYouPosts(

    ) {

        return ResponseEntity.ok(postService.getRecommendPosts());

    }


}
