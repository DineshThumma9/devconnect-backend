package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.LoginRequestDTO;
import com.pm.jujutsu.dtos.LoginResponseDTO;
import com.pm.jujutsu.dtos.UserResponseDTO;
import com.pm.jujutsu.mappers.UserMappers;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.Encoder;
import com.pm.jujutsu.utils.JwtUtil;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public JwtUtil jwtUtil;

    @Autowired
    public Encoder encoder;

    @Autowired
    public UserMappers userMappers;


    public static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    public Optional<LoginResponseDTO> login(LoginRequestDTO loginRequestDTO) {
        logger.info("🔍 Login attempt for email: " + loginRequestDTO.getEmail());
        
        Optional<com.pm.jujutsu.model.User> userOpt = userRepository.findByEmail(loginRequestDTO.getEmail());
        
        if (userOpt.isEmpty()) {
            logger.error("❌ User not found with email: " + loginRequestDTO.getEmail());
            return Optional.empty();
        }
        
        com.pm.jujutsu.model.User user = userOpt.get();
        logger.info("✅ User found: " + user.getEmail());
        
        boolean passwordMatches = encoder.matches(loginRequestDTO.getPassword(), user.getHashedPassword());
        logger.info("🔑 Password match result: " + passwordMatches);
        
        if (!passwordMatches) {
            logger.error("❌ Password does not match for user: " + user.getEmail());
            return Optional.empty();
        }
        
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        UserResponseDTO userResponseDTO = userMappers.toResponseEntity(user);
        
        System.out.println("✅ Login successful for: " + user.getEmail());
        return Optional.of(new LoginResponseDTO(accessToken, refreshToken, userResponseDTO));
    }


    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
