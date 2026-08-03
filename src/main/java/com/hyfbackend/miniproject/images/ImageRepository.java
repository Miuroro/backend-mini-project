package com.hyfbackend.miniproject.images;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class ImageRepository {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    public void save(UserImage userImage) {
        try {
            String tagsJson = objectMapper.writeValueAsString(userImage.getTags());

            jdbcClient
                    .sql("INSERT INTO user_images (id, user_id, image_url, uploaded_at, tags) VALUES (:id, :userId, :imageUrl, :uploadedAt, CAST(:tags AS jsonb))")
                    .param("id", userImage.getId())
                    .param("userId", userImage.getUserId())
                    .param("imageUrl", userImage.getImageUrl())
                    .param("uploadedAt", userImage.getUploadedAt())
                    .param("tags", tagsJson)
                    .update();
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize image tags to JSON", e);
        }
    }

    public List<UserImage> findByUserId(String userId) {
        return jdbcClient
                .sql("SELECT id, user_id, image_url, uploaded_at, tags FROM user_images WHERE user_id = :userId")
                .param("userId", userId)
                .query((rs, rowNum) -> {
                    Map<String, Object> tagsMap = null;
                    try {
                        String jsonTags = rs.getString("tags");
                        if (jsonTags != null) {
                            tagsMap = objectMapper.readValue(jsonTags, Map.class);
                        }
                    } catch (Exception e) {
                        tagsMap = Map.of();
                    }

                    return new UserImage(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getString("image_url"),
                            rs.getTimestamp("uploaded_at").toLocalDateTime(),
                            tagsMap
                    );
                })
                .list();
    }

    public UserImage findById(String id) {
        return jdbcClient
                .sql("SELECT id, user_id, image_url, uploaded_at, tags FROM user_images WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> {
                    Map<String, Object> tagsMap = null;
                    try {
                        String jsonTags = rs.getString("tags");
                        if (jsonTags != null) {
                            tagsMap = objectMapper.readValue(jsonTags, Map.class);
                        }
                    } catch (Exception e) {
                        tagsMap = Map.of();
                    }

                    return new UserImage(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getString("image_url"),
                            rs.getTimestamp("uploaded_at").toLocalDateTime(),
                            tagsMap
                    );
                })
                .optional()
                .orElse(null);
    }

    public void delete(String id) {
        jdbcClient
                .sql("DELETE FROM user_images WHERE id = :id")
                .param("id", id)
                .update();
    }

    // Search images by keyword inside the jsonb tags column
    public List<UserImage> searchByKeyword(String keyword, int limit, int offset) {
        String searchPattern = "%" + keyword.toLowerCase() + "%";
        return jdbcClient
                .sql("SELECT id, user_id, image_url, uploaded_at, tags FROM user_images WHERE LOWER(CAST(tags AS text)) LIKE :pattern LIMIT :limit OFFSET :offset")
                .param("pattern", searchPattern)
                .param("limit", limit)
                .param("offset", offset)
                .query((rs, rowNum) -> {
                    Map<String, Object> tagsMap = null;
                    try {
                        String jsonTags = rs.getString("tags");
                        if (jsonTags != null) {
                            tagsMap = objectMapper.readValue(jsonTags, Map.class);
                        }
                    } catch (Exception e) {
                        tagsMap = Map.of();
                    }

                    return new UserImage(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getString("image_url"),
                            rs.getTimestamp("uploaded_at").toLocalDateTime(),
                            tagsMap
                    );
                })
                .list();
    }
}
