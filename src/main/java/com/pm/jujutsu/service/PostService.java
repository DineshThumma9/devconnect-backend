package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.exceptions.UnauthorizedException;
import com.pm.jujutsu.mappers.PostMapper;
import com.pm.jujutsu.model.Comment;
import com.pm.jujutsu.model.Post;
import com.pm.jujutsu.model.PostNode;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.PostRepository;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.JwtUtil;
import org.bson.types.ObjectId;
import org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

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

    @Autowired
    private MultipleOpenApiSupportConfiguration multipleOpenApiSupportConfiguration;
    @Autowired
    private Neo4jTemplate neo4jTemplate;
    @Autowired
    private Neo4jClient neo4jClient;

    public PostResponseDTO createPost(PostRequestDTO postRequestDTO) {
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();
        Post post = postMapper.toEntity(postRequestDTO);
        post.setOwnerId(currentUserId); // Ensure the post is owned by current user


        PostNode postNode = new PostNode();
        postNode.setId(String.valueOf(post.getId()));
        neo4jService.syncPostTags(postNode.getId(), (Set<String>) post.getTags());



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

        Map<String,Object> postNode = neo4jService.getPostById(postId);




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

    public boolean increaseLike(String postId,String userId) {
        ObjectId objectId = new ObjectId(postId);
        Optional<Post> post = postRepository.findById(objectId);
        if(post.isPresent()){
            Post post1 = post.get();

            post1.setLikes(post1.getLikes()+1);
            neo4jService.createLikeRelationship(postId,userId);
            return true;
        }
        else {
            return false;
        }


    }


    public boolean decreaseLike(String postId,String userId) {
        ObjectId objectId = new ObjectId(postId);
        Optional<Post> post = postRepository.findById(objectId);
        if(post.isPresent()){
            Post post1 = post.get();
            post1.setLikes(post1.getLikes()-1);
            neo4jService.removeLikeRelationship(postId,userId);
            return true;
        }
        else {
            return false;
        }
    }


    public boolean commentOnPost(String postId,String userId,String comment){
        ObjectId postObjectId = new ObjectId(postId);
        ObjectId userObjectId = new ObjectId(userId);

        Optional<Post> post = postRepository.findById(postObjectId);
        Optional<User> user = userRepository.getById(userObjectId);
        if(post.isEmpty()){
            throw new NotFoundException("Post Doesnt Exist");
        }
        if (user.isEmpty()){
            throw new NotFoundException("User Not Found");
        }

        Post post1 = post.get();
        Comment comment1 = new Comment();
        comment1.setPostId(postId);
        comment1.setUserId(userObjectId);
        comment1.setComment(comment);
        post1.setCommentsCount((post1.getCommentsCount()+1);
        return true;


    }

    public boolean shareAPost(String postId){
        ObjectId objectId = new ObjectId(postId);
        Optional<Post>  post = postRepository.findById(objectId);
        if(post.isEmpty()){
            return false;
        }
        Post post1 = post.get();

        post1.setShares(post1.getShares()+1);
        return true;

    }



    public List<PostResponseDTO> getTrendingPost(){
        List<Post> posts = postRepository.findAllByLikes();
       List<PostResponseDTO> postResponseDTOS  = posts.stream().map(postMapper::toResponseEntity).toList();
        return postResponseDTOS;


    }


    public List<PostResponseDTO> getRecommendPosts(String userId){
        ObjectId objectId = new ObjectId(userId);
        Optional<User> userOpt = userRepository.findById(objectId);

        if(userOpt.isEmpty()){
            return List.of();
        }

        User user = userOpt.get();
        List<String> recommendPosts = neo4jService.recommendPostBasedOnTags(user.getInterests());
        List<String> recommendPostFromConnections = neo4jService.recommendPostBasedOnConnectionsAndTags(userId, user.getInterests());

        // Combine and remove duplicates
        recommendPosts.addAll(recommendPostFromConnections);
        List<String> uniqueUserIds = recommendPosts.stream().distinct().toList();


        List<ObjectId> objectIds = uniqueUserIds.stream()
                .map(ObjectId::new)
                .toList();

        // Fetch users from repository
        List<Post> posts = StreamSupport.stream(postRepository.findAllById(objectIds).spliterator(), false)
                .collect(Collectors.toList());

        // Map to DTOs
        return posts.stream()
                .map(postMapper::toResponseEntity)
                .collect(Collectors.toList());
    }



}
