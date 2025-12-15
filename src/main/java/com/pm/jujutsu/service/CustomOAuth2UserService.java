package com.pm.jujutsu.service;

import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

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
        String fullName = oauth2User.getAttribute("name");
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        if (email != null) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            
            if (existingUser.isEmpty()) {
                // Extract base username from email (part before @)
                String baseUsername = email.split("@")[0];
                
                // Generate random 3-digit number
                Random random = new Random();
                int randomNum = 100 + random.nextInt(900); // Generates 100-999
                
                // Combine to create unique username
                String username = baseUsername + randomNum;
                
                // Ensure uniqueness (very unlikely to collide, but safety check)
                while (userRepository.findByUsername(username).isPresent()) {
                    randomNum = 100 + random.nextInt(900);
                    username = baseUsername + randomNum;
                }
                
                // Create new user
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(username); // e.g., john.doe847
                newUser.setName(fullName != null ? fullName : baseUsername); // Use OAuth name or email prefix
                newUser.setProvider(provider); // "google" or "github"
                
                // OAuth users don't have password - they can ONLY login via OAuth
                // hashedPassword will be null - this prevents email/password login
                // If they want password login later, add a "Set Password" feature
                
                User savedUser = userRepository.save(newUser);
                
                // Create corresponding Neo4j node for social graph
                neo4jService.createUserNode(savedUser.getId().toHexString());
                
                System.out.println("✅ OAuth user created:");
                System.out.println("   Email: " + savedUser.getEmail());
                System.out.println("   Username: " + savedUser.getUsername());
                System.out.println("   Name: " + savedUser.getName());
                System.out.println("   Provider: " + savedUser.getProvider());
                System.out.println("   Password: null (OAuth-only login)");
            } else {
                System.out.println("ℹ️ OAuth user already exists: " + email);
            }
        }
        
        return oauth2User;
    }
}