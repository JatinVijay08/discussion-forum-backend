package com.jatin.forum.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Table(name="users")
public class User {
    @Getter
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable=false,unique=true)
    private String username;
    @Column(nullable=false,unique=true)
    private String password;
    @Getter
    @Column(nullable=false,unique=true)
    private String email;

    private Instant created;

    protected User() {}

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.created = Instant.now();
    }

    public String getPassword() {
        return password;
    }
}
