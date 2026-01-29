package com.jatin.forum.repository;

import java.util.List;
import com.jatin.forum.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepo extends JpaRepository<Post, Long> {


    List<Post> getPostByUserId(Long id);
}
