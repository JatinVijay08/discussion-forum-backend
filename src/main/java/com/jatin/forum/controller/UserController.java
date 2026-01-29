package com.jatin.forum.controller;

import com.jatin.forum.dto.PostResponse;
import com.jatin.forum.dto.UserResponse;
import com.jatin.forum.entity.User;
import com.jatin.forum.repository.PostRepo;
import com.jatin.forum.repository.UserRepo;
import com.jatin.forum.service.PostService;
import com.jatin.forum.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserRepo userRepo;
    private final PostRepo postRepo;
    private final PostService postService;

    public UserController(UserService userService, UserRepo userRepo, PostRepo postRepo, PostService postService) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.postRepo = postRepo;
        this.postService = postService;
    }

    @GetMapping()
    public UserResponse getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        return userService.findById(user.getId());
    }

    @GetMapping("/posts")
    public List<PostResponse> getPosts(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        return userService.getPosts(user.getId());
    }

    @DeleteMapping("/posts/{postId}")
    public void deletePost(@PathVariable Long postId){
         postService.deletePostById(postId);
    }

}
