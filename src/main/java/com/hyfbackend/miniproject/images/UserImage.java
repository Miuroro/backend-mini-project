package com.hyfbackend.miniproject.images;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class UserImage {
    private String id;
    private String userId;
    private String imageUrl;
    private LocalDateTime uploadedAt;
    private Map<String, Object> tags;
}