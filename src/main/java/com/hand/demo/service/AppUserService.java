package com.hand.demo.service;

import java.util.Collection;
import java.util.List;

import javax.security.auth.login.CredentialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hand.demo.model.Dtos.AppUserDto;
import com.hand.demo.model.repository.AppUserProjection;
import com.hand.demo.model.repository.AppUserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service

public class AppUserService implements UserDetailsService {


    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserDto(username);
    }

    protected UserDetails userAuthorization() throws CredentialException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CredentialException("user not found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails user) {
            return user;
        } else {
            throw new CredentialException("user not found");
        }
    }

    public AppUserDto loadUserDto(String username) {
        List<AppUserProjection> results;
        results = appUserRepository.findUserWithRoles(username);


        if (results.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        var first = results.get(0);
        Collection<GrantedAuthority> authorities = results.stream()
                .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                .collect(java.util.stream.Collectors.toList());
        return new AppUserDto(first.getUsername(), first.getPassword(),
               

                authorities);
    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
