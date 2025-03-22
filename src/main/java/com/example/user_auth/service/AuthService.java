package com.example.user_auth.service;

import com.example.user_auth.model.User;
import com.example.user_auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> registerUser(String username, String password, String role) {
        // Check if the user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        // Create new user and save it
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        User savedUser = userRepository.save(user);

        // Create a response map to return
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", savedUser.getId());
        userInfo.put("username", savedUser.getUsername());
        userInfo.put("role", savedUser.getRole());

        return userInfo;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User createGuestUser() {
        User guestUser = new User();
        guestUser.setUsername("guest_" + System.currentTimeMillis());
        guestUser.setPassword(passwordEncoder.encode("guest"));
        guestUser.setRole("ROLE_GUEST");
        return userRepository.save(guestUser);
    }
}
