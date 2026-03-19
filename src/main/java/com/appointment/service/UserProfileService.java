package com.appointment.service;

import com.appointment.model.UserProfile;
import com.appointment.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    @Autowired
    private UserProfileRepository userProfileRepository;

    public UserProfile createUserProfile(UserProfile userProfile) {
        UserProfile createdProfile = this.userProfileRepository.save(userProfile);
        // Fetch the profile with user relationship properly loaded
        UserProfile nProfile = this.userProfileRepository.findByUserIdWithUser(createdProfile.getUser().getId())
                .orElseThrow(() -> new RuntimeException("UserProfile not found"));
        return nProfile;
    }
}