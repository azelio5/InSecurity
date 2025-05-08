package com.is.authify.controllers;

import com.is.authify.io.ProfileRequest;
import com.is.authify.io.ProfileResponse;
import com.is.authify.services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@RequestBody @Valid ProfileRequest request) {
        ProfileResponse response = profileService.createProfile(request);
        //TODO: welcome email
        return response;
    }
}
