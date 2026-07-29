package com.hyfbackend.miniproject.images;

import com.hyfbackend.miniproject.service.R2StorageService;
import com.hyfbackend.miniproject.user.User;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final com.hyfbackend.miniproject.user.UserRepository userRepository;
    private final R2StorageService r2StorageService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserImage uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tags", required = false) Map<String, Object> tags,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a non-empty image file");
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }
        try {
            return imageService.uploadImage(file, user.getId(), tags);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
    }

    @GetMapping("/images")
    public List<UserImage> getUserImages(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }
        return imageService.getImagesForUser(user.getId());
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<Map<String, String>> deleteImage(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }

        imageService.deleteImage(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
    }

    @GetMapping("/files/{fileKey}")
    public ResponseEntity<byte[]> getPrivateImage(
            @PathVariable String fileKey,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        try {
            ResponseBytes<GetObjectResponse> objectBytes = r2StorageService.getFile(fileKey);
            MediaType contentType = MediaType.IMAGE_JPEG; // fallback
            String sdkContentType = objectBytes.response().contentType();
            if (sdkContentType != null) {
                contentType = MediaType.parseMediaType(sdkContentType);
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(objectBytes.asByteArray());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found in storage");
        }
    }
}