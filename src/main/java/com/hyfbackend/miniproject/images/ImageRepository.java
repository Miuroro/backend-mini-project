package com.hyfbackend.miniproject.images;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class ImageRepository {

    private final JdbcClient jdbcClient;

    public void save(UserImage userImage) {
        jdbcClient
                .sql("INSERT INTO user_images (id, user_id, image_url, uploaded_at, tags) VALUES (:id, :userId, :imageUrl, :uploadedAt, CAST(:tags AS jsonb))")
                .param("id", userImage.getId())
                .param("userId", userImage.getUserId())
                .param("imageUrl", userImage.getImageUrl())
                .param("uploadedAt", userImage.getUploadedAt())
                .param("tags", userImage.getTags())
                .update();
    }

    public List<UserImage> findByUserId(String userId) {
        return jdbcClient
                .sql("SELECT id, user_id, image_url, uploaded_at, tags FROM user_images WHERE user_id = :userId")
                .param("userId", userId)
                .query(UserImage.class)
                .list();
    }

    public UserImage findById(String id) {
        return jdbcClient
                .sql("SELECT id, user_id, image_url, uploaded_at, tags FROM user_images WHERE id = :id")
                .param("id", id)
                .query(UserImage.class)
                .optional()
                .orElse(null);
    }

    public void delete(String id) {
        jdbcClient
                .sql("DELETE FROM user_images WHERE id = :id")
                .param("id", id)
                .update();
    }
}
