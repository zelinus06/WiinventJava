package com.wiinventjava.Controller;

import com.wiinventjava.DTO.Request.UserRequest;
import com.wiinventjava.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        try {
            userService.createUser(request);
            return ResponseEntity.ok("User created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get/{username}")
    public ResponseEntity<?> getProfileUser(@PathVariable String username) {
        try {
            return ResponseEntity.ok(userService.getProfileUser(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/minus-points")
    public ResponseEntity<String> deductPoints(
            @RequestParam String username,
            @RequestParam int points) throws InterruptedException {
        userService.minusPoints(username, points);
        return ResponseEntity.ok("Đã trừ " + points + " điểm cho " + username);
    }
}
