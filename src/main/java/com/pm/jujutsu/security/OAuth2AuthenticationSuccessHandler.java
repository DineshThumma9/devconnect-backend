package com.pm.jujutsu.security;

import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        
        if (email != null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Generate JWT token
                String token = jwtUtil.generateToken(email);
                
                // Redirect to frontend with token
                String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth/callback")
                        .queryParam("token", token)
                        .queryParam("email", email)
                        .build().toUriString();
                
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }
        }
        
        // If user not found, redirect to error page
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/oauth/callback?error=user_not_found");
    }
}
