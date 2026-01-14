package com.jatin.forum.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class PostService {
    public String getAllPosts(){
        return "Service: Returning all posts";
    }

    public String createPost(){
        return "Service: Creating post";
    }

    public String getPostById(@PathVariable Long id){
        return "Service: returning post by id: "+id ;
    }



}
