package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.exceptions.UnauthorizedException;
import com.pm.jujutsu.mappers.PostMapper;
import com.pm.jujutsu.model.Post;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.PostRepository;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.JwtUtil;
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

    @Autowired
    public JwtUtil jwtUtil;

    public PostResponseDTO createPost(PostRequestDTO postRequestDTO) {
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();
        Post post = postMapper.toEntity(postRequestDTO);
        post.setOwnerId(currentUserId); // Ensure the post is owned by current user
        
        Post savedPost = postRepository.save(post);
        PostResponseDTO responseDTO = postMapper.toResponseEntity(savedPost);

        Optional<User> owner = userRepository.findById(savedPost.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());
        }

        return responseDTO;
    }

    public PostResponseDTO updatePost(PostRequestDTO postRequestDTO, String postId) {
        ObjectId objectId = new ObjectId(postId);
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();
        
        Post post = postRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Post Not Found"));

        if (!post.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedException("User not authorized to update this post");
        }

        post.setTitle(postRequestDTO.getTitle());
        post.setContent(postRequestDTO.getContent());

        Post savedPost = postRepository.save(post);
        PostResponseDTO responseDTO = postMapper.toResponseEntity(savedPost);

        Optional<User> owner = userRepository.findById(savedPost.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());
        }

        return responseDTO;
    }

    public PostResponseDTO getPost(String postId) {
        ObjectId objectId = new ObjectId(postId);
        Post post = postRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        PostResponseDTO responseDTO = postMapper.toResponseEntity(post);

        Optional<User> owner = userRepository.findById(post.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());
        }
        
        return responseDTO;
    }

    public boolean deletePost(String postId) {
        ObjectId objectId = new ObjectId(postId);
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();

        Post post = postRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedException("User not authorized to delete this post");
        }

        postRepository.deleteById(objectId);
        return true;
    }
}
