package com.jatin.forum.dto;

public record CreatePostRequest(
        String title,
        String content
) {}
