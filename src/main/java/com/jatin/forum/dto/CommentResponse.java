package com.jatin.forum.dto;

import com.jatin.forum.entity.Comment;

import java.time.Instant;

public record CommentResponse(
        String username,
        Long id,
        String content,
        String authorEmail,
        Instant createdAt,
        Comment ParentId
) {
}
