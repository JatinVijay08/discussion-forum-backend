package com.jatin.forum.service;

import com.jatin.forum.dto.CreatePostRequest;
import com.jatin.forum.dto.PostResponse;
import com.jatin.forum.entity.Post;
import com.jatin.forum.entity.User;
import com.jatin.forum.repository.PostRepo;
import com.jatin.forum.repository.UserRepo;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class PostService {
    private final UserRepo userRepo;
    private PostRepo postRepo;
    public PostService(PostRepo postRepo, UserRepo userRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
    }


    public List<PostResponse> getAllPosts(){
       return postRepo.findAll()
               .stream()
               .map(post->new PostResponse(post.getId(), post.getTitle(), post.getContent()))
               .toList();

    }

    public PostResponse createPost(CreatePostRequest createPostRequest){
        User user = userRepo.findById(1L).orElseThrow(()-> new RuntimeException("User not found"));
        Post post = new Post(createPostRequest.title(),  createPostRequest.content(),user);
        Post postSaved = postRepo.save(post);
        return new PostResponse(postSaved.getId(),postSaved.getTitle(),postSaved.getContent());
    }

    public PostResponse getPostById(Long id){
        Post post  = postRepo.findById(id).orElseThrow(()->new RuntimeException("post not found")) ;
        return  new PostResponse(post.getId(), post.getTitle(), post.getContent());

    }



}
