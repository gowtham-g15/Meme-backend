package com.example.memegenerator.controller;

import com.example.memegenerator.dto.LoginRequest;
import com.example.memegenerator.dto.SignupRequest;
import com.example.memegenerator.entity.User;
import com.example.memegenerator.repository.UserRepository;
import com.example.memegenerator.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody SignupRequest request) {
        try {
            System.out.println("Received signup request: " + request.getEmail() + ", " + request.getUsername());
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Username is required"));
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password must be at least 6 characters"));
            }
            if (userRepo.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email already registered"));
            }
            if (userRepo.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Username already taken"));
            }

            try {
                User user = new User();
                user.setUsername(request.getUsername().trim());
                user.setEmail(request.getEmail().trim().toLowerCase());
                user.setPassword(passwordEncoder.encode(request.getPassword()));

                System.out.println("Attempting to save user: " + user.getEmail());
                user = userRepo.save(user);
                System.out.println("User saved successfully with ID: " + user.getId());

                String token = jwtService.generateToken(user.getUsername());
                java.util.Map<String, Object> response = new java.util.HashMap<>();
                response.put("message", "User registered successfully");
                response.put("token", token);
                response.put("user", user);

                return ResponseEntity.ok(response);

            } catch (DataIntegrityViolationException e) {
                System.err.println("Database error during signup: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(createErrorResponse("A database error occurred. Please try a different username or email."));
            } catch (Exception e) {
                System.err.println("Unexpected error during signup: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(createErrorResponse("An unexpected error occurred. Please try again."));
            }

        } catch (Exception e) {
            System.err.println("Error processing signup request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("An error occurred while processing your request."));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password is required"));
            }

            User user = userRepo.findByEmail(request.getEmail().trim().toLowerCase())
                    .orElse(null);
                    
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body(createErrorResponse("Invalid email or password"));
            }

            String token = jwtService.generateToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(createErrorResponse("An error occurred during login. Please try again."));
        }
    }
}