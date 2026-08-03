package com.hyfbackend.miniproject.images;

import com.hyfbackend.miniproject.service.R2StorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ImageService {

    private final R2StorageService r2StorageService;
    private final ImageRepository imageRepository;
    private final ImageTaggingService imageTaggingService;

    public UserImage uploadImage(MultipartFile file, String userId, Map<String, Object> tags) throws IOException {
        String fileKey = r2StorageService.uploadFile(file);

        // Automatically trigger LLM tagging if no manual tags were provided
        Map<String, Object> finalTags = (tags == null || tags.isEmpty())
                ? imageTaggingService.generateTags(file)
                : tags;

        UserImage userImage = new UserImage(
                UUID.randomUUID().toString(),
                userId,
                fileKey,
                LocalDateTime.now(),
                finalTags
        );

        imageRepository.save(userImage);
        return userImage;
    }

    public List<UserImage> getImagesForUser(String userId) {
        return imageRepository.findByUserId(userId);
    }

    // Public method to get all images with pagination for the public gallary
    public List<UserImage> getAllImages(int page, int size) {
        int offset = page * size;
        return imageRepository.findAll(size, offset);
    }

    public void deleteImage(String imageId, String userId) {
        UserImage image = imageRepository.findById(imageId);
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
        // security check to ensure the image belongs to the logged-in user
        if (!image.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this image");
        }
        // Delete file from R2 storage
        if (image.getImageUrl() != null) {
            r2StorageService.deleteFile(image.getImageUrl());
        }

        imageRepository.delete(imageId);
    }

    // search images by a keyword with pagination
    public List<UserImage> searchImages(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        int offset = page * size;
        return imageRepository.searchByKeyword(keyword.trim(), size, offset);
    }
}