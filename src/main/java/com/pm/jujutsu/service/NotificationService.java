package com.pm.jujutsu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    



    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    

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

}
