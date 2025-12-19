package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.dtos.UserUpdateDTO;
import com.pm.jujutsu.exceptions.BadRequestException;
import com.pm.jujutsu.exceptions.ConflictException;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.exceptions.UnauthorizedException;
import com.pm.jujutsu.mappers.UserMappers;
import com.pm.jujutsu.model.User;

import com.pm.jujutsu.repository.UserNodeRepository;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.Encoder;
import com.pm.jujutsu.utils.JwtUtil;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private UserMappers userMappers;

    @Autowired
    private Encoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtils;

    @Autowired
    private SupabaseStorageService supabaseStorageService;



    @Autowired
    private Neo4jService neo4jService;
    @Autowired
    private UserNodeRepository userNodeRepository;

    public List<UserResponseDTO> getAllUser() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMappers::toResponseEntity)
                .collect(Collectors.toList());
    }

    public UserResponseDTO createUser(UserRequestDTO user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("User already exists");
        }

        User newUser = userMappers.toEntity(user);
        newUser.setHashedPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(newUser);

        // Create corresponding Neo4j node for social graph
        neo4jService.createUserNode(savedUser.getId().toHexString());

        // Sync interests to Neo4j only if they exist
        if (savedUser.getInterests() != null && !savedUser.getInterests().isEmpty()) {
            neo4jService.syncUserTags(
                savedUser.getId().toHexString(), 
                savedUser.getInterests()
            );
        }

        UserResponseDTO responseDTO = userMappers.toResponseEntity(savedUser);
        return responseDTO;
    }




    public UserResponseDTO updateUser(UserUpdateDTO user) {
        User updatedUser = userRepository.getUserByEmail(user.getEmail()).orElseThrow(
                () -> new NotFoundException("User doesnt exist")
        );

        if (!updatedUser.getId().equals(jwtUtils.getCurrentUser().getId())) {
            throw new UnauthorizedException("User not authorized");
        }

        // Update only provided fields
        if (user.getUsername() != null) {
            updatedUser.setUsername(user.getUsername());
        }
        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }
        if (user.getProfilePicUrl() != null) {
            updatedUser.setProfilePicUrl(user.getProfilePicUrl());
        }
        
        // Update interests if provided
        if (user.getInterests() != null) {
            updatedUser.setInterests(user.getInterests());
        }

        User savedUser = userRepository.save(updatedUser);
        
        // Sync interests to Neo4j after saving to MongoDB
        if (savedUser.getInterests() != null && !savedUser.getInterests().isEmpty()) {
            neo4jService.syncUserTags(savedUser.getId().toHexString(), savedUser.getInterests());
        }
        
        return userMappers.toResponseEntity(savedUser);
    }

    public UserResponseDTO updateProfilePicture(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        // Get current user
        User currentUser = userRepository.findById(jwtUtils.getCurrentUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Delete old profile picture if exists
        deleteOldProfilePicture(currentUser.getProfilePicUrl());

        // Upload new profile picture to Supabase Storage
        String profilePicUrl = supabaseStorageService.uploadFile(file);

        // Update user with new profile picture URL
        currentUser.setProfilePicUrl(profilePicUrl);
        User updatedUser = userRepository.save(currentUser);

        return userMappers.toResponseEntity(updatedUser);
    }

    private void deleteOldProfilePicture(String profilePicUrl) {
        if (profilePicUrl != null && profilePicUrl.contains(supabaseStorageService.getBucketUrl())) {
            try {
                // Extract filename from the URL
                String fileName = profilePicUrl.substring(profilePicUrl.lastIndexOf("/") + 1);
                supabaseStorageService.deleteFile(fileName);
            } catch (Exception e) {
                // Log error but don't halt execution
                System.err.println("Failed to delete old profile picture: " + e.getMessage());
            }
        }
    }

    public UserResponseDTO getUser(String id) {

        return userRepository.findById(new ObjectId(id))
                .map(userMappers::toResponseEntity)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }




    public void deleteUser(String uuid) {
        ObjectId currentUserId = jwtUtils.getCurrentUser().getId();
        ObjectId targetUserId = new ObjectId(uuid);


        if (!targetUserId.equals(currentUserId)) {
            throw new UnauthorizedException("User is not authorized to delete another user's account");
        }

        User userToDelete = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        


        userNodeRepository.deleteById(targetUserId.toHexString());
        deleteOldProfilePicture(userToDelete.getProfilePicUrl());
        userRepository.delete(userToDelete);

    }



    @Transactional
    public boolean addFollower(String targetUsername, String currentUserEmailOrUsername) {
        Optional<User> targetUser = userRepository.findByUsername(targetUsername);
        // Current user identifier could be email (from UserDetails) or username
        Optional<User> currentUser = userRepository.findByEmail(currentUserEmailOrUsername);
        if (currentUser.isEmpty()) {
            currentUser = userRepository.findByUsername(currentUserEmailOrUsername);
        }
        
        if (targetUser.isEmpty() || currentUser.isEmpty()) {
            return false;
        }
        
        User target = targetUser.get();
        User current = currentUser.get();
        
        target.getFollowerIds().add(current.getId());
        current.getFollowingIds().add(target.getId());
        
        userRepository.save(target);
        userRepository.save(current);
        
        neo4jService.followRelationship(current.getId().toHexString(), target.getId().toHexString());
        return true;
    }


    @Transactional
    public boolean removeFollower(String targetUsername, String currentUserEmailOrUsername) {
        Optional<User> targetUser = userRepository.findByUsername(targetUsername);
        // Current user identifier could be email (from UserDetails) or username
        Optional<User> currentUser = userRepository.findByEmail(currentUserEmailOrUsername);
        if (currentUser.isEmpty()) {
            currentUser = userRepository.findByUsername(currentUserEmailOrUsername);
        }
        
        if (targetUser.isEmpty() || currentUser.isEmpty()) {
            return false;
        }
        
        User target = targetUser.get();
        User current = currentUser.get();
        
        target.getFollowerIds().remove(current.getId());
        current.getFollowingIds().remove(target.getId());
        
        userRepository.save(target);
        userRepository.save(current);
        
        neo4jService.unfollowRelationship(current.getId().toHexString(), target.getId().toHexString());
        return true;
    }


    public List<UserResponseDTO> getRecommendConnections(String username){
        Optional<User> userOpt = userRepository.findByUsername(username);

        if(userOpt.isEmpty()){
            System.out.println("❌ User not found: " + username);
            return List.of();
        }

        User user = userOpt.get();
        String userId = user.getId().toHexString();
        
        System.out.println("🔍 Getting recommendations for user: " + username);
        System.out.println("   User ID: " + userId);
        System.out.println("   Interests: " + user.getInterests());
        
        if (user.getInterests() == null || user.getInterests().isEmpty()) {
            System.out.println("⚠️ User has no interests - cannot recommend connections");
            return List.of();
        }
        
        List<String> recommendUsers = neo4jService.getConnectionsBasedOnInterests(user.getInterests());
        List<String> recommendConnections = neo4jService.getConnectionsBasedOnConnectionsAndInterests(userId, user.getInterests());

        System.out.println("📊 Recommendations based on interests: " + recommendUsers.size());
        System.out.println("📊 Recommendations based on follows: " + recommendConnections.size());

        recommendConnections.addAll(recommendUsers);
        List<String> uniqueUserIds = recommendConnections.stream().distinct().toList();
        
        System.out.println("📊 Total unique recommendations: " + uniqueUserIds.size());

        List<ObjectId> objectIds = uniqueUserIds.stream()
                .map(ObjectId::new)
                .toList();


        List<User> users = StreamSupport.stream(userRepository.findAllById(objectIds).spliterator(), false)
                .collect(Collectors.toList());


        return users.stream()
                .map(userMappers::toResponseEntity)
                .collect(Collectors.toList());
    }



    public List<UserResponseDTO> getFollowers(String userIdOrUsername){
        User user = findUserByIdOrUsername(userIdOrUsername);
        
        Set<ObjectId> followerIds = user.getFollowerIds();
        List<User> followers = StreamSupport.stream(userRepository.findAllById(followerIds).spliterator(), false)
                .collect(Collectors.toList());

        return followers.stream()
                .map(userMappers::toResponseEntity)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> getFollowings(String userIdOrUsername){
        User user = findUserByIdOrUsername(userIdOrUsername);
        
        Set<ObjectId> followingIds = user.getFollowingIds();
        List<User> followings = StreamSupport.stream(userRepository.findAllById(followingIds).spliterator(), false)
                .collect(Collectors.toList());

        return followings.stream()
                .map(userMappers::toResponseEntity)
                .toList();
    }

    /**
     * Helper method to find user by either ObjectId or username
     */
    private User findUserByIdOrUsername(String userIdOrUsername) {
        try {
            // Try parsing as ObjectId first
            ObjectId objectId = new ObjectId(userIdOrUsername);
            return userRepository.findById(objectId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
        } catch (IllegalArgumentException e) {
            // If not a valid ObjectId, treat as username
            return userRepository.findByUsername(userIdOrUsername)
                    .orElseThrow(() -> new NotFoundException("User not found"));
        }
    }



}
