package com.example.demo.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Utilities;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class Authentication {

    @Autowired
    private UserRepository userRepository;

    private static final long SESSION_EXPIRY_SECONDS = 60 * 60 * 24; // 24 hours

    public User authenticate(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        Cookie userCookie = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Utilities.COOKIE_NAME)) {
                userCookie = cookie;
                break;
            }
        }

        if (userCookie == null) return null;

        User user = userRepository.findBySession(userCookie.getValue());

        if (user != null && !user.isDeleted()) {
            // Optional: check session expiry
            Instant lastLogin = user.getLastLoginAt();
            if (lastLogin != null) {
                Instant now = Instant.now();
                if (now.isAfter(lastLogin.plusSeconds(SESSION_EXPIRY_SECONDS))) {
                    // session expired
                    user.setSession(null);
                    userRepository.save(user);
                    return null;
                }
            }
            return user;
        }

        return null;
    }

    public User getLoggedInUser(HttpServletRequest request) {
        return authenticate(request);
    }
}
