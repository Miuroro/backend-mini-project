package com.hyfbackend.miniproject.user;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserRepository {

    private final JdbcClient jdbcClient;

    public void createUser(User user) {
        jdbcClient
                .sql("INSERT INTO users (id, username, password) VALUES (:id, :username, :password)")
                .param("id", user.getId())
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .update();
    }

    public User findByUsername(String username) {
        return jdbcClient
                .sql("SELECT id, username, password, avatar_url AS avatarUrl FROM users WHERE username = :username")
                .param("username", username)
                .query(User.class)
                .optional()
                .orElse(null);
    }

    public void updateAvatar(String userId, String avatarUrl) {
        jdbcClient
                .sql("UPDATE users SET avatar_url = :avatarUrl WHERE id = :id")
                .param("avatarUrl", avatarUrl)
                .param("id", userId)
                .update();
    }

    public void clearAvatar(String userId) {
        jdbcClient
                .sql("UPDATE users SET avatar_url = NULL WHERE id = :id")
                .param("id", userId)
                .update();
    }
}
