package com.hyfbackend.miniproject.images;

import com.hyfbackend.miniproject.service.R2StorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "*") // for the browser
@AllArgsConstructor
public class ImageSearchController {

    private final ImageService imageService;
    private final R2StorageService r2StorageService;

    // Public endpoint to get ALL uploaded images for the main gallery page
    @GetMapping
    public ResponseEntity<List<UserImage>> getAllImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<UserImage> results = imageService.getAllImages(page, size);

        // Convert raw storage keys into temporary viewable presigned URLs for private bucket access
        List<UserImage> enrichedResults = results.stream().map(img -> {
            String presignedUrl = r2StorageService.getPresignedUrl(img.getImageUrl());
            return new UserImage(
                    img.getId(),
                    img.getUserId(),
                    presignedUrl,
                    img.getUploadedAt(),
                    img.getTags()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(enrichedResults);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserImage>> searchImages(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) { // limit 50 images per page

        List<UserImage> results = imageService.searchImages(keyword, page, size);

        // Convert raw storage keys into temporary viewable presigned URLs for private bucket access
        List<UserImage> enrichedResults = results.stream().map(img -> {
            String presignedUrl = r2StorageService.getPresignedUrl(img.getImageUrl());
            return new UserImage(
                    img.getId(),
                    img.getUserId(),
                    presignedUrl,
                    img.getUploadedAt(),
                    img.getTags()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(enrichedResults);
    }
}