package com.pm.jujutsu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import com.pm.jujutsu.security.JwtHandShakeInterceptors;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
 
    @Autowired
    private JwtHandShakeInterceptors jwtHandShakeInterceptors;

  @Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/wss")
            .setAllowedOriginPatterns("http://localhost:5173")
            .addInterceptors(jwtHandShakeInterceptors)
            .withSockJS();
}


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic","/queue");
    }

}
