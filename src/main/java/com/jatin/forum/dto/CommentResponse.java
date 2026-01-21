package com.jatin.forum.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        String authorEmail,
        Instant createdAt
) {
}
