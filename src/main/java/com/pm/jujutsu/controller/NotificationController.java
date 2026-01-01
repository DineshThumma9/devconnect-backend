package com.pm.jujutsu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pm.jujutsu.dtos.NotificationResponseDTO;
import com.pm.jujutsu.service.NotificationService;


@Controller
@RequestMapping("/notifications")
public class NotificationController {
    

    @Autowired
    private NotificationService notificationService;




    @GetMapping("/{username}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsForUser(@PathVariable String username) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(username));
    }


    @PostMapping("/mark-as-read/{notificationId}")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
    


}
