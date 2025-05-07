package com.is.authify.services;

import com.is.authify.io.ProfileRequest;
import com.is.authify.io.ProfileResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest profileRequest);
}
