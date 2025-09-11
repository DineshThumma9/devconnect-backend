package com.pm.jujutsu.controller;


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


    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(
            @PathVariable String postId
    ) {


        PostResponseDTO post = postService.getPost(postId);
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();

    }


    @PostMapping("/create")
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody PostRequestDTO postRequestDTO) {


        return ResponseEntity.ok(postService.createPost(postRequestDTO));

    }


    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable("postId") String postId,
            @RequestBody PostRequestDTO postRequestDTO

    ) {

        return ResponseEntity.ok(postService.updatePost(postRequestDTO, postId));


    }


    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("postId") String postId
    ) {


        return postService.deletePost(postId) ?
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

            @PathVariable("postId") String postId,
            @RequestBody String comment

    ) {


        return postService.commentOnPost(postId, comment) ?
                ResponseEntity.ok().build() :
                ResponseEntity.notFound().build();

    }

    @PutMapping("/share/{postId}")
    public ResponseEntity<Void> share(
            @PathVariable("postId") String postId
    ) {

        return postService.shareAPost(postId) ?
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
