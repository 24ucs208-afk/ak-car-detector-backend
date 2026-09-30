package com.akcardetector.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.akcardetector.entity.User;
import com.akcardetector.repository.UserRepository;

@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET ALL USERS
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // CREATE USER / REGISTER
    @PostMapping("/api/users")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // =========================
    // LOGIN API
    // =========================
    @PostMapping("/api/login")
    public Map<String, Object> login(@RequestBody User user) {

        User existingUser =
                userRepository.findByEmailAndPassword(
                        user.getEmail(),
                        user.getPassword()
                );

        Map<String, Object> response = new HashMap<>();

        if (existingUser != null) {

            response.put("success", true);
            response.put("message", "Login successful");

            // IMPORTANT
            // Send logged-in user's ID to Flutter
            response.put("userId", existingUser.getId());

            // Also send complete user object
            response.put("user", existingUser);

        } else {

            response.put("success", false);
            response.put("message", "Invalid email or password");
        }

        return response;
    }

    // =========================
    // UPDATE USER
    // =========================
    @PutMapping("/api/users/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User user) {

        Optional<User> existingUser =
                userRepository.findById(id);

        if (existingUser.isPresent()) {

            User oldUser = existingUser.get();

            oldUser.setName(user.getName());
            oldUser.setEmail(user.getEmail());
            oldUser.setPassword(user.getPassword());
            oldUser.setPhone(user.getPhone());

            return userRepository.save(oldUser);
        }

        return null;
    }

    // =========================
    // DELETE USER
    // =========================
    @DeleteMapping("/api/users/{id}")
    public String deleteUser(@PathVariable Long id) {

        if (userRepository.existsById(id)) {

            userRepository.deleteById(id);

            return "User deleted successfully";
        }

        return "User not found";
    }
}