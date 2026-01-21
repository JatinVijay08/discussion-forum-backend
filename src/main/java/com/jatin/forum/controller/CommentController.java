package com.jatin.forum.controller;

import com.jatin.forum.dto.CommentResponse;
import com.jatin.forum.dto.CreateCommentRequest;
import com.jatin.forum.entity.Comment;
import com.jatin.forum.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/post/{postId}")
    public Comment addComment(@PathVariable("postId") Long postId, @RequestBody CreateCommentRequest createCommentRequest) {

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
       return commentService.CreateComment(postId, createCommentRequest.content(),  userEmail);

    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable("commentId") Long commentId) {
        String userEmail =  SecurityContextHolder.getContext().getAuthentication().getName();
        commentService.deleteComment(commentId, userEmail);
    }

    @GetMapping("/post/{postId}")
    public Page<CommentResponse> getCommentByPostId(@PathVariable("postId") Long postId,@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {

        Page<Comment> comment =  commentService.getCommentByPostId(postId, page, size);
         return comment.map(comment1 -> new CommentResponse(
                comment1.getId(),
                comment1.getContent(),
                comment1.getUser().getEmail(),
                comment1.getCreatedAt()
        ));

    }

}
