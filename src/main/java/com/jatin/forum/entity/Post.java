package com.jatin.forum.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String content;

    private Instant createdAt;

    public Long getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }


    public String getContent() {
        return content;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }


    public Post(String title, String content) {
        this.title = title;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public Post() {}


}
