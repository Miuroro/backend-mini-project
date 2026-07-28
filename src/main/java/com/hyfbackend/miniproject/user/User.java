package com.hyfbackend.miniproject.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class User {
    private String id;
    private String username;
    private String password;
    private String avatarUrl;
}
