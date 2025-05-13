package com.pm.jujutsu.controller;


import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/")
public class PostController {


    @Autowired
    public PostService postService;


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



}
