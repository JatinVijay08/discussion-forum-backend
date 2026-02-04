package com.jatin.forum.service;

import com.jatin.forum.dto.CreateCommentRequest;
import com.jatin.forum.entity.Comment;
import com.jatin.forum.entity.Post;
import com.jatin.forum.entity.User;
import com.jatin.forum.repository.CommentRepo;
import com.jatin.forum.repository.PostRepo;
import com.jatin.forum.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {

    private final PostRepo postRepo;
    private final CommentRepo commentRepo;
    private final UserRepo userRepo;
    public CommentService(PostRepo postRepo, CommentRepo commentRepo, UserRepo userRepo) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
    }

    public Comment CreateComment(Long postID, CreateCommentRequest createCommentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Optional<Post> post = postRepo.findById(postID);
        if (post.isEmpty()) {
            throw new RuntimeException("Post not found");
        }
        Comment comment = new Comment(createCommentRequest.content(), user, post.get());

        if(createCommentRequest.parentId() != null) {
            Comment parent = commentRepo.findById(createCommentRequest.parentId()).orElseThrow(()->new RuntimeException("Parent comment not found"));
            comment.setParentComment(parent);
        }
       return commentRepo.save(comment);
    }

    public void deleteComment(Long commentID) {
        Optional<Comment> comment = commentRepo.findById(commentID);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);
        if (comment.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }
        if(!comment.get().getUser().getEmail().equals(user.getEmail())) {
        throw new RuntimeException("You are not allowed to delete this comment");
        }
        commentRepo.delete(comment.get());
    }

    public Page<Comment> getCommentByPostId(Long postId, int page,int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,"createdAt"));
        return commentRepo.findByPostId(postId, pageable);
    }

}
