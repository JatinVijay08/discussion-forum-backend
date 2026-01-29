package com.jatin.forum.service;

import com.jatin.forum.JwtUtil;
import com.jatin.forum.dto.LoginRequest;
import com.jatin.forum.dto.LoginResponseDto;
import com.jatin.forum.dto.RegisterRequest;
import com.jatin.forum.entity.User;
import com.jatin.forum.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepo userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest registerRequest) {

        String hashedPassword = passwordEncoder.encode(registerRequest.password());
        User user = new User(registerRequest.username(),hashedPassword,registerRequest.email()
        );

        userRepo.save(user);
    }

    public LoginResponseDto login(LoginRequest loginRequest) {
        User user = userRepo.findByEmail(loginRequest.email());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(user);
        String username = user.getUsername();
        return new LoginResponseDto(token,username);
    }




}
