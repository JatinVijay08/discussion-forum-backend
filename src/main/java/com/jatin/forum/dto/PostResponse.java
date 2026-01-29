package com.jatin.forum.dto;

import com.jatin.forum.entity.VoteType;

public record PostResponse(
        String username,
        Long id,
        String title,
        String content,
        long voteCount,
        long commentCount,
        VoteType userVote
){}
