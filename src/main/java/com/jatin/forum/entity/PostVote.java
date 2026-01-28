package com.jatin.forum.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_vote",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id","user_id"})
})
public class PostVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name="post_id",nullable = false)
    Post post;

    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    public PostVote() {
    }
    public PostVote(User user, Post post, VoteType voteType) {
        this.user = user;
        this.post = post;
        this.voteType = voteType;
    }

    public VoteType getVoteType() {
        return voteType;
    }
    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
}
