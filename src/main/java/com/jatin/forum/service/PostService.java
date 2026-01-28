package com.jatin.forum.service;

import com.jatin.forum.dto.CreatePostRequest;
import com.jatin.forum.dto.PostResponse;
import com.jatin.forum.entity.Post;
import com.jatin.forum.entity.PostVote;
import com.jatin.forum.entity.User;
import com.jatin.forum.entity.VoteType;
import com.jatin.forum.repository.PostRepo;
import com.jatin.forum.repository.PostVoteRepo;
import com.jatin.forum.repository.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class PostService {
    private final UserRepo userRepo;
    private final PostVoteRepo postVoteRepo;
    private PostRepo postRepo;
    public PostService(PostRepo postRepo, UserRepo userRepo, PostVoteRepo postVoteRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
        this.postVoteRepo = postVoteRepo;
    }


    public List<PostResponse> getAllPosts(){
       return postRepo.findAll()
               .stream()
               .map(this::mapToPostResponse)
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
        return mapToPostResponse(savedPost);
    }

    public PostResponse getPostById(Long id){
        Post post  = postRepo.findById(id).orElseThrow(()->new RuntimeException("post not found")) ;
        return  mapToPostResponse(post);

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

    public PostResponse mapToPostResponse(Post post){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        if(user==null){
            throw new RuntimeException("User not found");
        }
        long upvotes = postVoteRepo.countByPostAndVoteType(post, VoteType.upvote);
        long downvotes = postVoteRepo.countByPostAndVoteType(post, VoteType.downvote);

        VoteType voteType = postVoteRepo.findByUserAndPost(user,post).map(PostVote::getVoteType).orElse(null);
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(), upvotes, downvotes, voteType);

    }



}
