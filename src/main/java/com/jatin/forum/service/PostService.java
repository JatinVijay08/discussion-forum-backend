package com.jatin.forum.service;

import com.jatin.forum.dto.CreatePostRequest;
import com.jatin.forum.dto.PostResponse;
import com.jatin.forum.entity.Post;
import com.jatin.forum.repository.PostRepo;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class PostService {
    private PostRepo postRepo;
    public PostService(PostRepo postRepo) {
        this.postRepo = postRepo;
    }


    public List<PostResponse> getAllPosts(){
       return postRepo.findAll()
               .stream()
               .map(post->new PostResponse(post.getId(), post.getTitle(), post.getContent()))
               .toList();

    }

    public PostResponse createPost(CreatePostRequest createPostRequest){
        Post post = new Post(createPostRequest.title(),  createPostRequest.content());
        Post postSaved = postRepo.save(post);
        return new PostResponse(postSaved.getId(),postSaved.getTitle(),postSaved.getContent());
    }

    public PostResponse getPostById(Long id){
        Post post  = postRepo.findById(id).orElseThrow(()->new RuntimeException("post not found")) ;
        return  new PostResponse(post.getId(), post.getTitle(), post.getContent());

    }



}
