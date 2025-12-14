package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
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
            neo4jService.createUserInterestsRelationship(
                savedUser.getId().toHexString(), 
                savedUser.getInterests()
            );
        }

        UserResponseDTO responseDTO = userMappers.toResponseEntity(savedUser);
        return responseDTO;
    }




    public UserResponseDTO updateUser(UserRequestDTO user) {
        User updatedUser = userRepository.getUserByEmail(user.getEmail()).orElseThrow(
                () -> new NotFoundException("User doesnt exist")
        );

        if (!updatedUser.getId().equals(jwtUtils.getCurrentUser().getId())) {
            throw new UnauthorizedException("User not authorized");
        }

        updatedUser.setUsername(user.getUsername());
        updatedUser.setName(user.getName());
        
        // Update interests if provided
        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            updatedUser.setInterests(user.getInterests());
        }

        User newUser = userRepository.save(updatedUser);
        
        // Sync interests to Neo4j if they were updated
        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            neo4jService.syncUserTags(newUser.getId().toHexString(), user.getInterests());
        }
        
        return userMappers.toResponseEntity(newUser);
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
    public boolean addFollower(String userId,String ownerId ){
        ObjectId ownerObjectId = new ObjectId(userId);
        ObjectId userObjectId = new ObjectId(ownerId);
        Optional<User> owner = userRepository.getById(ownerObjectId);
        Optional<User> user = userRepository.getById(userObjectId);
        if(user.isEmpty() || owner.isEmpty()){
            return false;
        }
        User owner1 = owner.get();
        owner1.getFollowingIds().add(userObjectId);
        neo4jService.followRelationship(userId,ownerId);
        return true;
    }


    @Transactional
    public boolean removeFollower(String userId,String ownerId ){
        ObjectId ownerObjectId = new ObjectId(userId);
        ObjectId userObjectId = new ObjectId(ownerId);
        Optional<User> owner = userRepository.getById(ownerObjectId);
        Optional<User> user = userRepository.getById(userObjectId);
        if(user.isEmpty() || owner.isEmpty()){
            return false;
        }
        User owner1 = owner.get();
        owner1.getFollowerIds().add(userObjectId);
        neo4jService.unfollowRelationship(userId,ownerId);
        return true;
    }


    public List<UserResponseDTO> getRecommendConnections(String userId){
        ObjectId objectId = new ObjectId(userId);
        Optional<User> userOpt = userRepository.findById(objectId);

        if(userOpt.isEmpty()){
            return List.of();
        }

        User user = userOpt.get();
        List<String> recommendUsers = neo4jService.getConnectionsBasedOnInterests(user.getInterests());
        List<String> recommendConnections = neo4jService.getConnectionsBasedOnConnectionsAndInterests(userId, user.getInterests());




        recommendConnections.addAll(recommendUsers);
        List<String> uniqueUserIds = recommendConnections.stream().distinct().toList();

        List<ObjectId> objectIds = uniqueUserIds.stream()
                .map(ObjectId::new)
                .toList();


        List<User> users = StreamSupport.stream(userRepository.findAllById(objectIds).spliterator(), false)
                .collect(Collectors.toList());


        return users.stream()
                .map(userMappers::toResponseEntity)
                .collect(Collectors.toList());
    }



    public List<UserResponseDTO> getFollowers(String userId){
        ObjectId objectId = new ObjectId(userId);
        Optional<User> userOpt = userRepository.findById(objectId);

        if(userOpt.isEmpty()){
            return List.of();
        }

        User user = userOpt.get();
        Set<ObjectId> followerIds = user.getFollowerIds();

        List<User> followers = StreamSupport.stream(userRepository.findAllById(followerIds).spliterator(), false)
                .collect(Collectors.toList());

        return followers.stream()
                .map(userMappers::toResponseEntity)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> getFollowings(String userId){
        ObjectId objectId = new ObjectId(userId);
        Optional<User> userOpt = userRepository.findById(objectId);

        if(userOpt.isEmpty()){
            return List.of();
        }

        User user = userOpt.get();
        Set<ObjectId> followingIds = user.getFollowingIds();

        List<User> followings = StreamSupport.stream(userRepository.findAllById(followingIds).spliterator(), false)
                .collect(Collectors.toList());

        return followings.stream()
                .map(userMappers::toResponseEntity)
                .collect(Collectors.toList());
    }



}
