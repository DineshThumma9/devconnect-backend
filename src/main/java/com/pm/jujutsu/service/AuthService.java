package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.LoginRequestDTO;
import com.pm.jujutsu.dtos.LoginResponseDTO;
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

    public Optional<LoginResponseDTO> login(LoginRequestDTO loginRequestDTO) {
        return userRepository.findByEmail(loginRequestDTO.getEmail())
                .filter(user -> encoder.matches(loginRequestDTO.getPassword(), user.getHashedPassword()))
                .map(user -> {
                    String accessToken = jwtUtil.generateToken(user.getEmail());
                    String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
                    return new LoginResponseDTO(accessToken, refreshToken);
                });
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
