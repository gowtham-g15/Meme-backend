package com.example.memegenerator.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "memes")
public class Meme {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topText;
    private String bottomText;
    private String imageUrl;
    private String uploadOption; 

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
