package com.pm.jujutsu.security;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import com.pm.jujutsu.service.JwtUserDetailsService;
import com.pm.jujutsu.utils.JwtUtil;
import java.security.Principal;

@Component
public class JwtHandShakeInterceptors implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtHandShakeInterceptors.class);


// ...existing imports...

@Override
public boolean beforeHandshake(
    ServerHttpRequest request,
    ServerHttpResponse response,
    WebSocketHandler wsHandler,
    Map<String, Object> attributes
) {
    String path = request.getURI().getPath();
    if (!"/wss".equals(path)) {
        return true;
    }
    try {
        String jwt = extractToken(request);
        if (jwt == null || jwt.isEmpty()) {
            logger.warn("No JWT token found in WebSocket handshake request");
            return false;
        }
        String username = jwtUtil.extractUsername(jwt);
        if (username == null) {
            logger.warn("Could not extract username from JWT");
            return false;
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtUtil.isTokenValid(jwt, userDetails)) {
            logger.warn("Invalid JWT token for user: {}", username);
            return false;
        }
        attributes.put("username", username);
        attributes.put("userDetails", userDetails);

        // Set Principal for WebSocket session
        attributes.put("principal", (Principal) () -> username);

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        logger.info("WebSocket authentication successful for user: {}", username);
        return true;
    } catch (Exception e) {
        logger.error("Error during WebSocket handshake authentication", e);
        return false;
    }
}
    private String extractToken(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("access_token=")) {
                    return param.substring("access_token=".length());
                }
            }
        }
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        @Nullable Exception exception
    ) {
        if (exception != null) {
            logger.error("WebSocket handshake completed with exception", exception);
        } else {
            logger.info("WebSocket handshake completed successfully");
        }
    }
}