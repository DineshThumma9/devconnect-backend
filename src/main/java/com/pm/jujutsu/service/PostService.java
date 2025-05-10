package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.mappers.PostMapper;
import com.pm.jujutsu.model.Post;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.PostRepository;
import com.pm.jujutsu.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostService {

    @Autowired
    public PostRepository postRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PostMapper postMapper;

    public PostResponseDTO createPost(String ownerId ,PostRequestDTO postRequestDTO)  {



        Post post = postMapper.toEntity(postRequestDTO);


        if(ownerId == null){
             throw new IllegalAccessException("Owner id is null");
        }

        System.out.println(post.getOwnerId());
        PostResponseDTO responseDTO = postMapper.toResponseEntity(post);


        Optional<User> owner = userRepository.findById(post.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());
        }

        return responseDTO;
    }

    public PostResponseDTO updatePost(PostRequestDTO postRequestDTO, String postId) {
        ObjectId objectId = new ObjectId(postId); // Convert String to ObjectId
        Post post = postRepository.findById(objectId).orElseThrow(
                () -> new IllegalArgumentException("Post Not Found")
        );
        post.setTitle(postRequestDTO.getTitle());
        post.setContent(postRequestDTO.getContent());
        post = postRepository.save(post);
        System.out.println(post.getOwnerId());

        PostResponseDTO responseDTO = postMapper.toResponseEntity(post);

        Optional<User> owner = userRepository.findById(post.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());
        }

        return responseDTO;
    }

    public PostResponseDTO getPost(String postId) {
        ObjectId objectId = new ObjectId(postId); // Convert String to ObjectId
        Post post = postRepository.findById(objectId).orElse(null);
        if (post != null) {
            PostResponseDTO responseDTO = postMapper.toResponseEntity(post);

            Optional<User> owner = userRepository.findById(post.getOwnerId());
            if (owner.isPresent()) {
                responseDTO.setOwnerUsername(owner.get().getUsername());
                responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());
            }
            return responseDTO;
        }
        return null;
    }

    public boolean deletePost(String postId) {
        ObjectId objectId = new ObjectId(postId); // Convert String to ObjectId
        if (postRepository.findById(objectId).isPresent()) {
            postRepository.deleteById(objectId);
            return true;
        }
        return false;
    }
}
