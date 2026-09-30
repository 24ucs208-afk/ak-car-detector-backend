package com.akcardetector.controller;

import com.akcardetector.entity.Feedback;
import com.akcardetector.repository.FeedbackRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackRepository repository;

    public FeedbackController(FeedbackRepository repository) {
        this.repository = repository;
    }

    // Get all feedback
    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(repository.findAll());
    }

    // Submit feedback
    @PostMapping
    public ResponseEntity<Feedback> submitFeedback(
            @RequestBody Feedback feedback) {

        if (feedback.getRating() < 1 ||
            feedback.getRating() > 5) {

            return ResponseEntity.badRequest().build();
        }

        if (feedback.getComment() == null ||
            feedback.getComment().trim().isEmpty()) {

            return ResponseEntity.badRequest().build();
        }

        Feedback savedFeedback =
                repository.save(feedback);

        return ResponseEntity.ok(savedFeedback);
    }

    // Delete feedback
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFeedback(
            @PathVariable Long id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);

        return ResponseEntity.ok(
                "Feedback deleted successfully"
        );
    }
}