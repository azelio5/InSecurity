package com.is.authify.io;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AuthResponse {
    private String email;
    private String token;
}
