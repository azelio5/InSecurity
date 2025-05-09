package com.is.authify.controllers;

import com.is.authify.io.AuthRequest;
import com.is.authify.io.AuthResponse;
import com.is.authify.services.AppUserDetailsService;
import com.is.authify.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody final AuthRequest authRequest) {
        try {
            authenticate(authRequest.getEmail(), authRequest.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
            final String jwtToken = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(authRequest.getEmail(), jwtToken));

        } catch (BadCredentialsException e) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", true);
            //          errors.put("message", e.getMessage());
            errors.put("message", "Email or password is incorrect");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);

        } catch (DisabledException e) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", true);
            errors.put("message", "Account is disabled");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);

        } catch (Exception e) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("error", true);
            errors.put("message", "Authentication is failed");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
        }
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
    }
}
