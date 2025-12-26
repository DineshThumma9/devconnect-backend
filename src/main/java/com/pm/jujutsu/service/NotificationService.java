package com.pm.jujutsu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    


    private SimpMessagingTemplate messagingTemplate;

    public void onPostLiked(String userId, String postId){
        messagingTemplate.convertAndSendToUser(userId, "/topic/notifications", "Your post " + postId + " was liked!");
    }


    public void onCommentAdded(String postId, String commentId){
        messagingTemplate.convertAndSend("/topic/posts/" + postId + "/comments", "New comment " + commentId + " added to post " + postId);
    }


    public void onNewMessage(){
        messagingTemplate.convertAndSendToUser("saalar", "/topic/test", "New message from " + "Hello");
    }
    
    public void onNewFollower(String followerId){

    }

    public void onMessageReceiveds(String senderId, String receiverId){

    }
  
    public void sendNotification(String message) {
        messagingTemplate.convertAndSend("/topic/notifications", "Hello Subscribers!");  
}

    public void onPosted(){

    }
}
