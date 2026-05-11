package com.example.bookmarking.repository;

import com.example.bookmarking.entity.Bookmark;
import com.example.bookmarking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    long countByUser(User user);

    Page<Bookmark> findByUser(User user, Pageable pageable);

    Page<Bookmark> findByUserAndTitleContainingIgnoreCaseOrUserAndUrlContainingIgnoreCase(
            User user1, String title,
            User user2, String url,
            Pageable pageable
    );
}
