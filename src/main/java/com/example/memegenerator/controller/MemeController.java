package com.example.memegenerator.controller;

import com.example.memegenerator.entity.Meme;
import com.example.memegenerator.entity.User;
import com.example.memegenerator.repository.MemeRepository;
import com.example.memegenerator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/memes")
@RequiredArgsConstructor
public class MemeController {

    private final MemeRepository memeRepo;
    private final UserRepository userRepo;

    @PostMapping
    public ResponseEntity<?> createMeme(@RequestBody Meme meme, Principal principal) {
        String username = principal.getName();
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");

        meme.setUser(user);
        Meme saved = memeRepo.save(meme);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/my")
    public List<Meme> getMyMemes(Principal principal) {
        String username = principal.getName();
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        return memeRepo.findByUserId(user.getId());
    }
}
