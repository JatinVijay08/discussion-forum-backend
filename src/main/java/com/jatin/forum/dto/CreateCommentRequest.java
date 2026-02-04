package com.jatin.forum.dto;

import com.jatin.forum.entity.Comment;

public record CreateCommentRequest(
        String content,
        Long parentId
) {
}
