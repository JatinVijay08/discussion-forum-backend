package com.jatin.forum.Controller;

import com.jatin.forum.Service.PostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String getAllPosts() {
        return postService.getAllPosts() ;
    }

    @PostMapping
    public String createPost(){
        return postService.createPost() ;
    }

    @GetMapping("{id}")
    public String getPostById(@PathVariable Long id){
        return postService.getPostById(id);
    }

}
