package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.CommentResponseDTO;
import com.pm.jujutsu.dtos.PostRequestDTO;
import com.pm.jujutsu.dtos.PostResponseDTO;
import com.pm.jujutsu.exceptions.BadRequestException;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.exceptions.UnauthorizedException;
import com.pm.jujutsu.mappers.PostMapper;
import com.pm.jujutsu.model.*;
import com.pm.jujutsu.repository.CommentRepository;
import com.pm.jujutsu.repository.NotificationRepository;
import com.pm.jujutsu.repository.PostNodeRespository;
import com.pm.jujutsu.repository.PostRepository;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.JwtUtil;
import jakarta.transaction.Transactional;

import org.aspectj.weaver.ast.Not;
import org.bson.types.ObjectId;
import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public PostNodeRespository postNodeRepository;

    @Autowired
    public Neo4jService neo4jService;

    @Autowired
    public CommentRepository commentRepository;


    @Autowired
    public SupabaseStorageService supabaseStorageService;


    @Autowired
    public NotificationService notificationService;


    @Autowired 
    private NotificationRepository notificationRepository;

    public final static String CACHE_POSTS = "posts";

    

    @CachePut(cacheNames = CACHE_POSTS, key = "#result.postId")
    public PostResponseDTO createPost(PostRequestDTO postRequestDTO, List<MultipartFile> images) throws IOException {
        ObjectId currentUserId = jwtUtil.getCurrentUser().getId();
        Post post = postMapper.toEntity(postRequestDTO);
        post.setOwnerId(currentUserId); // Ensure the post is owned by current user
        
        // Upload images if provided
        if (images != null && !images.isEmpty()) {
            List<String> mediaUrls = supabaseStorageService.uploadMultipleFiles(images, "posts");
            post.setMedia(mediaUrls.toArray(new String[0]));
        }


        Post savedPost = postRepository.save(post);
        
        // Create PostNode in Neo4j (without relationships)
        neo4jService.createPostNode(post.getId().toHexString());
        
        // Sync tags
        neo4jService.syncPostTags(post.getId().toHexString(), post.getTags());

        PostResponseDTO responseDTO = postMapper.toResponseEntity(savedPost);
        enrichPostResponse(savedPost, responseDTO);

        return responseDTO;
    }


    @CachePut(cacheNames = CACHE_POSTS, key = "#result.postId")
    public PostResponseDTO updatePost(PostRequestDTO postRequestDTO, String postId) {
        if (!ObjectId.isValid(postId)) {
            throw new BadRequestException("Invalid post ID format");
        }
        
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
        enrichPostResponse(savedPost, responseDTO);

        return responseDTO;
    }


    @Cacheable(cacheNames = CACHE_POSTS, key = "#postId")
    public PostResponseDTO getPost(String postId) {
        if (!ObjectId.isValid(postId)) {
            throw new BadRequestException("Invalid post ID format");
        }
        
        ObjectId objectId = new ObjectId(postId);
        Post post = postRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        PostResponseDTO responseDTO = postMapper.toResponseEntity(post);
        enrichPostResponse(post, responseDTO);

        return responseDTO;
    }
    

    @CacheEvict(cacheNames = CACHE_POSTS, key = "#postId")
    public boolean deletePost(String postId) {
        if (!ObjectId.isValid(postId)) {
            throw new BadRequestException("Invalid post ID format");
        }
        
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
    @CacheEvict(cacheNames = CACHE_POSTS, key = "#postId")
    public boolean increaseLike(String postId) {
        if (!ObjectId.isValid(postId)) {
            throw new BadRequestException("Invalid post ID format");
        }
        
        ObjectId objectId = new ObjectId(postId);
        Optional<Post> post = postRepository.findById(objectId);
        
        if (post.isPresent()) {
            Post post1 = post.get();
            Optional<User> userOpt = userRepository.findById(post1.getOwnerId());

            ObjectId userId = jwtUtil.getCurrentUser().getId();
            
            // Check if user has already liked this post
            if (post1.getLikedBy().contains(userId)) {
                return false; // Already liked, don't duplicate
            }
            
            // Add user to likedBy set
            post1.getLikedBy().add(userId);
            post1.setLikes(post1.getLikes() + 1);
            Notification notification = Notification.builder()
                .userId(userOpt.get().getId().toHexString())
                .type(NotificationType.LIKE)
                .message(jwtUtil.getCurrentUser().getUsername() + " liked your post.")
                .isRead(false)
                .timestamp(System.currentTimeMillis())
                .build();
            notificationRepository.save(notification);
            notificationService.onLikingPost(postId, jwtUtil.getCurrentUsername(), userOpt.get().getUsername());
            neo4jService.createLikeRelationship(postId, String.valueOf(userId));
            postRepository.save(post1);
            return true;
        } else {
            return false;
        }


    }


    @Transactional
    @CacheEvict(cacheNames = CACHE_POSTS, key = "#postId")
    public boolean decreaseLike(String postId) {
        if (!ObjectId.isValid(postId)) {
            throw new BadRequestException("Invalid post ID format");
        }
        
        ObjectId objectId = new ObjectId(postId);
        Optional<Post> post = postRepository.findById(objectId);
        if (post.isPresent()) {
            Post post1 = post.get();
            ObjectId userId = jwtUtil.getCurrentUser().getId();
            
            // Check if user has actually liked this post
            if (!post1.getLikedBy().contains(userId)) {
                return false; // User hasn't liked it, can't unlike
            }
            
            // Remove user from likedBy set
            post1.getLikedBy().remove(userId);
            post1.setLikes(post1.getLikes() - 1);
            neo4jService.removeLikeRelationship(postId, String.valueOf(userId));
            postRepository.save(post1);
            return true;
        } else {
            return false;
        }
    }



    @Transactional
    @CacheEvict(cacheNames = CACHE_POSTS, key = "#postId")
    public boolean commentOnPost(String postId, String comment) {
        // if (!ObjectId.isValid(postId)) {
        //     throw new BadRequestException("Invalid post ID format");
        // }
        
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
        comment1.setPostId(postObjectId);
        comment1.setUserId(userObjectId);
        comment1.setComment(comment);


        Optional<User> userOpt = userRepository.findById(post1.getOwnerId());
        
        // Save the comment to database
        Comment savedComment = commentRepository.save(comment1);
        
        // Create Neo4j relationship: User -[:COMMENTED]-> Comment -[:ON]-> Post
        neo4jService.createCommentRelationship(
            userObjectId.toHexString(),
            savedComment.getId().toHexString(),
            postObjectId.toHexString()
        );

        Notification notification = Notification.builder()
            .userId(userOpt.get().getId().toHexString())
            .type(NotificationType.COMMENT)
            .message(jwtUtil.getCurrentUser().getUsername() + " commented on your post.")
            .isRead(false)
            .timestamp(System.currentTimeMillis())
            .build();
        notificationRepository.save(notification);
        notificationService.onCommentingPost(postId, jwtUtil.getCurrentUser().getUsername(), userOpt.get().getUsername(), comment);
        post1.setCommentsCount(post1.getCommentsCount() + 1);
        postRepository.save(post1);
        return true;
    }


    @CacheEvict(cacheNames = CACHE_POSTS, key = "#postId")
    public boolean shareAPost(String postId) {
        if (!ObjectId.isValid(postId)) {
            throw new BadRequestException("Invalid post ID format");
        }
        
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


    @Cacheable(cacheNames = CACHE_POSTS, key = "'trendingPosts'")
    public List<PostResponseDTO> getTrendingPost() {
        List<Post> posts = postRepository.findAllByOrderByLikesDesc();
        
        return posts.stream()
                .map(post -> {
                    PostResponseDTO responseDTO = postMapper.toResponseEntity(post);
                    enrichPostResponse(post, responseDTO);
                    return responseDTO;
                })
                .toList();
    }


    @Cacheable(cacheNames = CACHE_POSTS, key = "'recommendPosts#' + #root.target.jwtUtil.currentUser.username")
    public List<PostResponseDTO> getRecommendPosts() {
        ObjectId objectId = jwtUtil.getCurrentUser().getId();
        Optional<User> userOpt = userRepository.findById(objectId);

        if (userOpt.isEmpty()) {
            return List.of();
        }

        User user = userOpt.get();
        List<String> recommendPosts =
                neo4jService.recommendPostBasedOnTags(jwtUtil.getCurrentUser().getId().toHexString(), user.getInterests());


        List<String> recommendPostFromConnections =
                neo4jService.recommendPostBasedOnConnectionsAndTags(jwtUtil.getCurrentUser().getId().toHexString(), user.getInterests());



        recommendPosts.addAll(recommendPostFromConnections);
        List<String> uniquePostIds = recommendPosts.stream().distinct().toList();

        // Convert String IDs to ObjectIds
        List<ObjectId> objectIds = uniquePostIds.stream()
                .map(ObjectId::new)
                .toList();

        // Fetch users from repository
        List<Post> posts = postRepository.findAllById(objectIds).stream()
                .toList();



        return posts.stream()
                .map(post -> {
                    PostResponseDTO responseDTO = postMapper.toResponseEntity(post);
                    enrichPostResponse(post, responseDTO);
                    return responseDTO;
                })
                .collect(Collectors.toList());
    }



    @Cacheable(cacheNames = CACHE_POSTS, key = "'userPosts#' + #username")
    public List<PostResponseDTO> getPostsByUserId(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ObjectId objectId = user.getId();
        List<Post> posts = postRepository.findAll().stream()
                .filter(post -> post.getOwnerId().equals(objectId))
                .toList();
        
        return posts.stream()
                .map(post -> {
                    PostResponseDTO responseDTO = postMapper.toResponseEntity(post);
                    enrichPostResponse(post, responseDTO);
                    return responseDTO;
                })
                .toList();
    }


    public CommentResponseDTO toResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        Optional<User> userOpt = userRepository.findById(comment.getUserId());
        if(!userOpt.isPresent()){
            throw new NotFoundException("User Not Found");
        }
        User user = userOpt.get();
        dto.setId(comment.getId().toHexString());
        dto.setComment(comment.getComment());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUsername(user.getUsername());
        dto.setUserProfilePicUrl(user.getProfilePicUrl());
        return dto;
    }


    @Cacheable(cacheNames = CACHE_POSTS, key = "'comment#' + #postId")
    public List<CommentResponseDTO> getCommentsOnPost(String postId) {
        if (!ObjectId.isValid(postId)) {
            throw new BadRequestException("Invalid post ID format");
        }
        
        ObjectId postObjectId = new ObjectId(postId);
        List<Comment> commentsOpt = commentRepository.findByPostId(postObjectId);
        List<CommentResponseDTO> commentResponseDTOS = commentsOpt.stream().map(comment -> toResponseDTO(comment)).toList();
        return commentResponseDTOS;
    
        
    }

    /**
     * Helper method to set isLikedByCurrentUser flag in PostResponseDTO
     */
    private void setIsLikedByCurrentUser(Post post, PostResponseDTO responseDTO) {
        try {
            ObjectId currentUserId = jwtUtil.getCurrentUser().getId();
            responseDTO.setLikedByCurrentUser(post.getLikedBy().contains(currentUserId));
        } catch (Exception e) {
            // If user is not authenticated, default to false
            responseDTO.setLikedByCurrentUser(false);
        }
    }

    /**
     * Helper method to enrich PostResponseDTO with owner information and like status
     */
    private void enrichPostResponse(Post post, PostResponseDTO responseDTO) {
        // Set owner information
        Optional<User> owner = userRepository.findById(post.getOwnerId());
        if (owner.isPresent()) {
            responseDTO.setOwnerUsername(owner.get().getUsername());
            responseDTO.setOwnerProfilePicUrl(owner.get().getProfilePicUrl());
        }
        // Set if current user liked this post
        setIsLikedByCurrentUser(post, responseDTO);
    }

}
