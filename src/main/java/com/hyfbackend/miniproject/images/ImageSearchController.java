package com.hyfbackend.miniproject.images;

import com.hyfbackend.miniproject.service.R2StorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/images")
@AllArgsConstructor
public class ImageSearchController {

    private final ImageService imageService;
    private final R2StorageService r2StorageService;

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