package com.appointment.service;

import com.appointment.model.User;
import com.appointment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(User user){
        User createdUser = this.userRepository.save(user);
        // Use the new method that fetches BOTH location and role
        User nUser = this.userRepository.findUserWithLocationAndRole(createdUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return nUser;
    }
}