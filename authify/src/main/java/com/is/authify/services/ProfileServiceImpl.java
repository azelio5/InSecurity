package com.is.authify.services;

import com.is.authify.entity.UserEntity;
import com.is.authify.io.ProfileRequest;
import com.is.authify.io.ProfileResponse;
import com.is.authify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);
        if (!repository.existsByEmail(request.getEmail())) {
            newProfile = repository.save(newProfile);
            return convertToProfileResponse(newProfile);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = repository.findByEmail(email).
                orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return convertToProfileResponse(existingUser);
    }

    @Override
    public void setResetOTO(String email) {
        UserEntity existingUser = repository.findByEmail(email).
                orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        //Generate 6 digit OTP

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 1000000));

        //Calculate expiring time (current time + 15 minutes in milliseconds)
        long expiringTime = System.currentTimeMillis() + (15 * 60 * 1000);

        //update profile entity
        existingUser.setResetOtp(otp);
        existingUser.setResetOtpExpireAt(expiringTime);

        //save to DB

        repository.save(existingUser);

        try {
            //TODO send the reset OTP otp
        } catch (Exception e) {
            throw new RuntimeException("Unable to sent an email");
        }

    }

    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .userId(newProfile.getUserId())
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }
}
