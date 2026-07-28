package com.hyfbackend.miniproject.user;

import com.hyfbackend.miniproject.user.dto.UserRequest;
import com.hyfbackend.miniproject.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(UserRequest request) {
        String hashedPassword = passwordEncoder.encode(request.password());

        String userId = UUID.randomUUID().toString();
        User newUser = new User(userId, request.username(), hashedPassword, null);
        userRepository.createUser(newUser);

        return new UserResponse(newUser.getId(), newUser.getUsername(), newUser.getAvatarUrl());
    }

    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return new UserResponse(user.getId(), user.getUsername(), user.getAvatarUrl());
    }
}
