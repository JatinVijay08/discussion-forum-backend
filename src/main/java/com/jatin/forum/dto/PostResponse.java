package com.jatin.forum.dto;

import com.jatin.forum.entity.VoteType;

public record PostResponse(
        Long id,
        String title,
        String content,
        long upvotes,
        long downvotes,
        VoteType userVote
){}
