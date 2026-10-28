package com.example.expenseapproval.service;

import com.example.expenseapproval.exception.ApiException;
import com.example.expenseapproval.model.AppUser;
import com.example.expenseapproval.model.Role;
import com.example.expenseapproval.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository users, BCryptPasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser login(String email, String password) {
        AppUser user = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        return user;
    }

    public AppUser currentUser(HttpSession session) {
        if (session == null || !(session.getAttribute("USER_ID") instanceof Long userId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please log in to continue.");
        }
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in again."));
    }

    public void logout(HttpSession session) {
        currentUser(session);
        session.invalidate();
    }

    public static void requireRole(AppUser user, Role role) {
        if (user.getRole() != role) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Your role cannot perform this action.");
        }
    }
}
