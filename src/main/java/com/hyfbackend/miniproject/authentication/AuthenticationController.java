package com.hyfbackend.miniproject.authentication;

import com.hyfbackend.miniproject.authentication.dto.LoginRequest;
import com.hyfbackend.miniproject.authentication.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request,
                               HttpServletRequest httpRequest) {
        return authenticationService.login(request, httpRequest.getSession());
    }

    @PostMapping("/logout")
    public LoginResponse logout(HttpSession session) {
        authenticationService.logout(session);
        return new LoginResponse("Logout successful");
    }
}
