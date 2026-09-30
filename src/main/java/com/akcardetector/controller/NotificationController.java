package com.akcardetector.controller;

import com.akcardetector.entity.Notification;
import com.akcardetector.repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // Get notifications for a user
    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(
            @PathVariable Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Create a new notification
    @PostMapping
    public Notification createNotification(
            @RequestBody Notification notification) {

        return notificationRepository.save(notification);
    }

    // Mark notification as read
    @PutMapping("/{id}/read")
    public Notification markAsRead(
            @PathVariable Long id) {

        Notification notification =
                notificationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    // Delete notification
    @DeleteMapping("/{id}")
    public void deleteNotification(
            @PathVariable Long id) {

        notificationRepository.deleteById(id);
    }
}