package com.pm.jujutsu.service;

import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final Neo4jService neo4jService;

    CustomOAuth2UserService(UserRepository userRepository, Neo4jService neo4jService) {
        this.userRepository = userRepository;
        this.neo4jService = neo4jService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Process OAuth2 user and save/update in database
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        if (email != null) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            
            if (existingUser.isEmpty()) {
                // Create new user
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name != null ? name : email);
                newUser.setProvider(provider);
                User savedUser = userRepository.save(newUser);
                
                // Create corresponding Neo4j node for social graph
                neo4jService.createUserNode(savedUser.getId().toHexString());
            }
        }
        
        return oauth2User;
    }
}