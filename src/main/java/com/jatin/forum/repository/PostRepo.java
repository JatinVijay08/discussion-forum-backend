package com.jatin.forum.repository;

import com.jatin.forum.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepo extends JpaRepository<Post, Long> {

    
}
