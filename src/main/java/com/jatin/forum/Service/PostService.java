package com.jatin.forum.Service;

import com.jatin.forum.DTO.CreatePostRequest;
import com.jatin.forum.DTO.PostResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class PostService {
    public String getAllPosts(){
        return "Service: Returning all posts";
    }

    public PostResponse createPost(CreatePostRequest createPostRequest){
       return new PostResponse(1L,createPostRequest.title(),createPostRequest.content());

    }

    public String getPostById(@PathVariable Long id){
        return "Service: returning post by id: "+id ;
    }



}
