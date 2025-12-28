package com.pm.jujutsu.security;

import java.security.Principal;
import java.util.Map;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import com.pm.jujutsu.service.JwtUserDetailsService;
import com.pm.jujutsu.utils.JwtUtil;

@Configuration
public class SecurityChannelConfig implements WebSocketMessageBrokerConfigurer {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private JwtUserDetailsService userDetailsService;
    
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SecurityChannelConfig.class);
    @Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor != null && accessor.getUser() == null) {
                Principal principal = (Principal) accessor.getSessionAttributes().get("principal");
                if (principal != null) {
                    accessor.setUser(principal);
                }
            }
            return message;
        }
    });
}
}