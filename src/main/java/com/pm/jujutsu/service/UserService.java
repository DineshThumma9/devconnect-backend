package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.UserRequestDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.mappers.UserMappers;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.Encoder;
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

    public List<UserResponseDTO> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMappers::toResponseEntity)
                .collect(Collectors.toList());
    }

    public UserResponseDTO createUser(UserRequestDTO user) throws IllegalAccessException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User newUser = userMappers.toEntity(user);
        newUser.setHashedPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(newUser);
        UserResponseDTO responseDTO =  userMappers.toResponseEntity(savedUser);
        return responseDTO;
    }

    public UserResponseDTO updateUser(UserRequestDTO user) throws IllegalAccessException {
        User updatedUser = userRepository.getUserByEmail(user.getEmail()).orElseThrow(
                () -> new IllegalAccessException("User doesnt exist")
        );

        updatedUser.setUsername(user.getUsername());
        updatedUser.setName(user.getName());
        updatedUser.setProfilePicUrl(user.getProfile_pic());
        User newUser = userRepository.save(updatedUser);

        return userMappers.toResponseEntity(newUser);
    }

    public UserResponseDTO getUser(String id) {
        return userRepository.findById(new ObjectId(id))
                .map(userMappers::toResponseEntity)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void deleteUser(String uuid) {
        User userToDelete = userRepository.getById(new ObjectId(uuid))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(userToDelete);
    }
}