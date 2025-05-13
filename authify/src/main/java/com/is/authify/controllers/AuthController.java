package com.is.authify.controllers;

import com.is.authify.io.AuthRequest;
import com.is.authify.io.AuthResponse;
import com.is.authify.io.ResetPasswordRequest;
import com.is.authify.services.AppUserDetailsService;
import com.is.authify.services.ProfileService;
import com.is.authify.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;


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

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(
            @CurrentSecurityContext(expression = "authentication.name") String email) {
        return ResponseEntity.ok(email != null);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            profileService.resetPassword(resetPasswordRequest.getEmail(),
                    resetPasswordRequest.getOtp(),
                    resetPasswordRequest.getNewPassword());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        try {
            profileService.sendResetOTO(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/send-otp")
    public void sendVerifyOtp(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            profileService.sendOTP(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
