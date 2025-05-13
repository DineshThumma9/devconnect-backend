package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.exceptions.BadRequestException;
import com.pm.jujutsu.exceptions.ConflictException;
import com.pm.jujutsu.exceptions.NotFoundException;
import com.pm.jujutsu.exceptions.UnauthorizedException;
import com.pm.jujutsu.mappers.UserMappers;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.Encoder;
import com.pm.jujutsu.utils.JwtUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        UserResponseDTO responseDTO = userMappers.toResponseEntity(savedUser);
        return responseDTO;
    }

    public UserResponseDTO updateUser(UserRequestDTO user) {

        User updatedUser = userRepository.getUserByEmail(user.getEmail()).orElseThrow(
                () -> new NotFoundException("User doesnt exist")
        );

        updatedUser.setUsername(user.getUsername());
        updatedUser.setName(user.getName());
        updatedUser.setProfilePicUrl(user.getProfile_pic());

        if (!updatedUser.getId().equals(jwtUtils.getCurrentUser().getId())) {
            throw new UnauthorizedException("User not authorized");
        }

        User newUser = userRepository.save(updatedUser);
        return userMappers.toResponseEntity(newUser);
    }

    public UserResponseDTO getUser(String id) {

        return userRepository.findById(new ObjectId(id))
                .map(userMappers::toResponseEntity)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public void deleteUser(String uuid) {
        ObjectId currentUserId = jwtUtils.getCurrentUser().getId();
        ObjectId targetUserId = new ObjectId(uuid);

        // Only allow users to delete their own account
        if (!targetUserId.equals(currentUserId)) {
            throw new UnauthorizedException("User is not authorized to delete another user's account");
        }

        User userToDelete = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        userRepository.delete(userToDelete);
    }
}
