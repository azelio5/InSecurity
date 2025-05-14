package com.is.authify.services;

import com.is.authify.io.ProfileRequest;
import com.is.authify.io.ProfileResponse;

public interface ProfileService {

    ProfileResponse createProfile(ProfileRequest profileRequest);

    ProfileResponse getProfile(String email);

    void sendResetOTO(String email);

    void resetPassword(String email, String otp, String newPassword);

    void sendOTP(String email);

    void verifyOtp(String email, String otp);

}
