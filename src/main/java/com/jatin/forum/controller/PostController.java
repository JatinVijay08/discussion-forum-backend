package com.jatin.forum.controller;

import com.jatin.forum.dto.CreatePostRequest;
import com.jatin.forum.dto.PostResponse;
import com.jatin.forum.service.PostService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postService.getAllPosts() ;
    }

    @PostMapping
    public PostResponse createPost(@RequestBody CreatePostRequest createPostRequest) {
        return postService.createPost(createPostRequest) ;
    }

    @GetMapping("{id}")
    public PostResponse getPostById(@PathVariable Long id){
        return postService.getPostById(id);
    }

}
