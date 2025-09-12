package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.exceptions.UnauthorizedException;
import com.pm.jujutsu.mappers.PostMapper;
import com.pm.jujutsu.model.*;
import com.pm.jujutsu.repository.PostRepository;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.JwtUtil;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;
import org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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


    @Autowired
    public Neo4jService neo4jService;





    public PostResponseDTO createPost(PostRequestDTO postRequestDTO) {
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();
        Post post = postMapper.toEntity(postRequestDTO);
        post.setOwnerId(currentUserId); // Ensure the post is owned by current user
        Post savedPost = postRepository.save(post);
        PostNode postNode = new PostNode();
        postNode.setId(post.getId());
        neo4jService.syncPostTags(postNode.getId(),  post.getTags());



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

        Optional<PostNode> postNode = neo4jService.getPostById(objectId);






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


    @Transactional
    public boolean increaseLike(String postId) {
        ObjectId objectId = new ObjectId(postId);
        Optional<Post> post = postRepository.findById(objectId);
        if (post.isPresent()) {
            Post post1 = post.get();

            post1.setLikes(post1.getLikes() + 1);
            ObjectId userId = jwtUtil.getCurrentUser().getId();
            neo4jService.createLikeRelationship(postId, String.valueOf(userId));
            postRepository.save(post1);
            return true;
        } else {
            return false;
        }


    }


    @Transactional
    public boolean decreaseLike(String postId) {
        ObjectId objectId = new ObjectId(postId);
        Optional<Post> post = postRepository.findById(objectId);
        if (post.isPresent()) {
            Post post1 = post.get();
            post1.setLikes(post1.getLikes() - 1);
            ObjectId userId = jwtUtil.getCurrentUser().getId();
            neo4jService.removeLikeRelationship(postId, String.valueOf(userId));
            postRepository.save(post1);
            return true;
        } else {
            return false;
        }
    }



    @Transactional
    public boolean commentOnPost(String postId, String comment) {
        ObjectId postObjectId = new ObjectId(postId);
        ObjectId userObjectId = jwtUtil.getCurrentUser().getId();

        Optional<Post> post = postRepository.findById(postObjectId);
        Optional<User> user = userRepository.getById(userObjectId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post Doesnt Exist");
        }
        if (user.isEmpty()) {
            throw new NotFoundException("User Not Found");
        }

        Post post1 = post.get();
        Comment comment1 = new Comment();
        comment1.setPostId(postId);
        comment1.setUserId(userObjectId);
        comment1.setComment(comment);

        post1.setCommentsCount((post1.getCommentsCount() + 1));
        postRepository.save(post1);
        return true;


    }

    public boolean shareAPost(String postId) {
        ObjectId objectId = new ObjectId(postId);
        Optional<Post> post = postRepository.findById(objectId);
        if (post.isEmpty()) {
            return false;
        }
        Post post1 = post.get();

        post1.setShares(post1.getShares() + 1);
        postRepository.save(post1);
        return true;

    }


    public List<PostResponseDTO> getTrendingPost() {
        List<Post> posts = postRepository.findAllByLikes();
        return posts.stream().map(postMapper::toResponseEntity).toList();


    }


    public List<PostResponseDTO> getRecommendPosts() {
        ObjectId objectId = jwtUtil.getCurrentUser().getId();
        Optional<User> userOpt = userRepository.findById(objectId);

        if (userOpt.isEmpty()) {
            return List.of();
        }

        User user = userOpt.get();
        List<ObjectId> recommendPosts =
                neo4jService.recommendPostBasedOnTags(jwtUtil.getCurrentUser().getId(),user.getInterests());


        List<ObjectId> recommendPostFromConnections =
                neo4jService.recommendPostBasedOnConnectionsAndTags(String.valueOf(jwtUtil.getCurrentUser().getId()), user.getInterests());



        recommendPosts.addAll(recommendPostFromConnections);
        List<ObjectId> uniqueUserIds = recommendPosts.stream().distinct().toList();




        // Fetch users from repository
        List<Post> posts = postRepository.findAllById(uniqueUserIds).stream()
                .toList();



        return posts.stream()
                .map(postMapper::toResponseEntity)
                .collect(Collectors.toList());
    }


}
