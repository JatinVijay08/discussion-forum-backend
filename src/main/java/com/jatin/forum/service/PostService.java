package com.jatin.forum.service;

import com.jatin.forum.dto.CreatePostRequest;
import com.jatin.forum.dto.PostResponse;
import com.jatin.forum.entity.Post;
import com.jatin.forum.entity.User;
import com.jatin.forum.repository.PostRepo;
import com.jatin.forum.repository.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        if(user==null){
            throw new RuntimeException("User not found");
        }
        Post post = new Post(createPostRequest.title(), createPostRequest.content(), user);

        Post savedPost = postRepo.save(post);
        return new PostResponse(savedPost.getId(), savedPost.getTitle(), savedPost.getContent());
    }

    public PostResponse getPostById(Long id){
        Post post  = postRepo.findById(id).orElseThrow(()->new RuntimeException("post not found")) ;
        return  new PostResponse(post.getId(), post.getTitle(), post.getContent());

    }

    public void deletePostById(Long postId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        if(user==null){
            throw new RuntimeException("User not found");
        }
        Post post = postRepo.findById(postId).orElseThrow(()->new RuntimeException("post not found"));
        if(!post.getUser().getId().equals(user.getId())){
           throw new RuntimeException("Not allowed to delete post");
        }
        postRepo.delete(post);

    }



}
