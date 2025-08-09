package com.hand.demo.auth;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.AppUser;

@Service
public class AuthorizedUserService {

    public Optional<AppUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AppUser user) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

}
