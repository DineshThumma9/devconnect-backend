package com.pm.jujutsu.controller;


import com.azure.core.annotation.Get;
import com.azure.core.annotation.Put;
import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.model.Post;
import com.pm.jujutsu.repository.PostRepository;
import com.pm.jujutsu.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/")
public class PostController {


    @Autowired
    public PostService postService;
    @Autowired
    private PostRepository postRepository;


    @GetMapping("/get-post/{postId}/")
    public ResponseEntity<PostResponseDTO> getPost(
            @PathVariable String postId
    ){
        PostResponseDTO postResponseDTO  = postService.getPost(postId);

        if(postResponseDTO != null){
            return ResponseEntity.ok(postResponseDTO);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/create/")
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody PostRequestDTO postRequestDTO){

        PostResponseDTO postResponseDTO = null;
            postResponseDTO = postService.createPost(postRequestDTO);

        return ResponseEntity.ok(postResponseDTO);

    }


    @PutMapping("/update/{postId}/")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable("postId") String postId,
            @RequestBody  PostRequestDTO postRequestDTO

    ){
        PostResponseDTO postResponseDTO = postService.updatePost(postRequestDTO,postId);
        return  ResponseEntity.ok(postResponseDTO);



    }


    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("postId") String postId
    ){

        if(postService.deletePost(postId)){
            return ResponseEntity.ok().build();
        }
        else {

            return ResponseEntity.notFound().build();
        }

    }


    @PutMapping("/{postId}/like-post")
    public ResponseEntity<Void> likeAPost(
            @PathVariable("postId") String postId
    ){

        return postService.increaseLike(postId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

    }

    @PutMapping("/unlike-post")
    public ResponseEntity<Void> unlikePost(
            @PathVariable("postId") String postId
    ){
        return postService.decreaseLike(postId) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{postId}/comment")
    public ResponseEntity<Void> commentOnPost(

            @PathVariable("postId") String postId,
            @RequestBody String userId,
            @RequestBody String comment

    ){


         return postService.commentOnPost(postId,userId,comment) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

    }






    @GetMapping("/trending-posts")
    public ResponseEntity<PostResponseDTO> getTrendingPost(

    ){

    }


    @GetMapping("/for-you-post")
    public ResponseEntity<PostResponseDTO> forYouPosts(){

    }




}
