package com.pm.jujutsu.service;

import java.util.List;
import java.util.Optional;

import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pm.jujutsu.dtos.NotificationResponseDTO;
import com.pm.jujutsu.model.Notification;
import com.pm.jujutsu.model.User;
import com.pm.jujutsu.repository.NotificationRepository;
import com.pm.jujutsu.repository.UserRepository;

@Service
public class NotificationService {
    



    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    
    @Autowired
    private NotificationRepository notificationRepository;


    @Autowired
    private UserRepository userRepository;


    public void onLikingPost(String postId,String likerUsername,String postOwnerUsername){
        String notificationMessage = likerUsername + " liked your post with ID: " + postId;
        messagingTemplate.convertAndSend(
           "/queue/user/notifications/" + postOwnerUsername ,
            notificationMessage
        );
    }


    public void onCommentingPost(String postId, String commenterUsername, String postOwnerUsername, String commentText) {
        String notificationMessage = commenterUsername + " commented on your post with ID: " + postId + ": " + commentText;
        messagingTemplate.convertAndSend(
           "/queue/user/notifications/" + postOwnerUsername ,
            notificationMessage
        );
    }

    public void onNewFollower(String followedUsername, String followerUsername) {
        String notificationMessage = followerUsername + " started following you.";
        messagingTemplate.convertAndSend(
           "/queue/user/notifications/" + followedUsername ,
            notificationMessage
        );
    }

    public void onNewMessage(String recipientUsername, String senderUsername) {
        String notificationMessage = "New message from " + senderUsername;
        messagingTemplate.convertAndSend(
           "/queue/user/notifications/" + recipientUsername ,
            notificationMessage
        );
    }


    public NotificationResponseDTO toResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
            .id(notification.getId().toHexString())
            .type(notification.getType().toString())
            .message(notification.getMessage())
            .isRead(notification.isRead())
            .timestamp(notification.getTimestamp())
            .build();
    }



    public List<NotificationResponseDTO> getNotificationsForUser(String username) {
        // TOgenerated method stub
         Optional<User> userOpt = userRepository.findByUsername(username);
         if (!userOpt.isPresent()) {
                throw new UsernameNotFoundException("User not found with username: " + username);
        }
        List<Notification> notifications = notificationRepository.findAllByUserId(userOpt.get().getId().toHexString());


        List<NotificationResponseDTO> dtoList= notifications.stream().map(this::toResponseDTO).toList();
        return dtoList;


        

    }

}
