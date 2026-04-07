package com.example.demo.auth;

import com.example.demo.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private Authentication authentication;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        // Only protect /admin/**, /host/**, /customer/** routes
        if (uri.startsWith("/admin") || uri.startsWith("/host") || uri.startsWith("/customer")) {

            User user = authentication.getLoggedInUser(request);

            if (user == null) {
                response.sendRedirect("/login");
                return false;
            }

            // Role-based access
            if ((uri.startsWith("/admin") && user.getType().toString().equals("ADMIN")) ||
                (uri.startsWith("/host") && user.getType().toString().equals("HOST")) ||
                (uri.startsWith("/customer") && user.getType().toString().equals("CUSTOMER"))) {
                return true; // allowed
            }

            // If role mismatch
            response.sendRedirect("/login");
            return false;
        }

        return true; // allow other URLs
    }
}
