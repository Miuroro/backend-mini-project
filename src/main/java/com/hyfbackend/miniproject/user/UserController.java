package com.hyfbackend.miniproject.user;

import com.hyfbackend.miniproject.service.R2StorageService;
import com.hyfbackend.miniproject.user.dto.AvatarDeleteResponse;
import com.hyfbackend.miniproject.user.dto.AvatarResponse;
import com.hyfbackend.miniproject.user.dto.UserRequest;
import com.hyfbackend.miniproject.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final R2StorageService r2StorageService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody UserRequest request) {
        return userService.register(request);
    }

    @GetMapping("/profile")
    public UserResponse profile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userService.getProfile(userDetails.getUsername());
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a non-empty image file");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }
        String fileKey;
        try {
            fileKey = r2StorageService.uploadFile(file);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }
        userRepository.updateAvatar(user.getId(), fileKey);
        return new UserResponse(user.getId(), user.getUsername(), fileKey);
    }

    @GetMapping("/avatar")
    public AvatarResponse getAvatar(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }
        return new AvatarResponse(user.getAvatarUrl());
    }

    @DeleteMapping("/avatar")
    public AvatarDeleteResponse deleteAvatar(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        User user = userRepository.findByUsername(userDetails.getUsername());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found");
        }
        if (user.getAvatarUrl() != null) {
            r2StorageService.deleteFile(user.getAvatarUrl());
        }

        userRepository.clearAvatar(user.getId());

        return new AvatarDeleteResponse("Avatar deleted successfully");
    }
}
