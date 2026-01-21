package com.jatin.forum.service;

import com.jatin.forum.entity.Comment;
import com.jatin.forum.entity.Post;
import com.jatin.forum.entity.User;
import com.jatin.forum.repository.CommentRepo;
import com.jatin.forum.repository.PostRepo;
import com.jatin.forum.repository.UserRepo;
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

    public Comment CreateComment(Long postID, String content, String userEmail) {
        User user = userRepo.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Optional<Post> post = postRepo.findById(postID);
        if (post.isEmpty()) {
            throw new RuntimeException("Post not found");
        }
        Comment comment = new Comment(content, user, post.get());
        return commentRepo.save(comment);
    }

    public void deleteComment(Long commentID, String userEmail) {
        Optional<Comment> comment = commentRepo.findById(commentID);
        if (comment.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }
        if(!comment.get().getUser().getEmail().equals(userEmail)) {
        throw new RuntimeException("You are not allowed to delete this comment");
        }
        commentRepo.delete(comment.get());
    }

}
