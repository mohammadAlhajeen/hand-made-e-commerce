package com.hand.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

import com.hand.auth.AuthorizedUserService;
import com.hand.model.Dtos.AppUserDto;
import com.hand.model.repository.AppUserProjection;
import com.hand.model.repository.AppUserRepository;
import java.util.ArrayList;

@Service

public class AppUserService implements UserDetailsService {

    @Autowired
    private AuthorizedUserService authorized;

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       return loadUserDto(username);
    }

    protected Optional<UserDetails> userAuthorization() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("user not found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails user) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    public AppUserDto loadUserDto(String username) {
        List<AppUserProjection> results ;
               results= appUserRepository.findUserWithRoles(username);

        if (results.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        AppUserProjection first = results.get(0);

        // نجمع الأدوار في Collection<GrantedAuthority>
        Collection<GrantedAuthority> authorities = results.stream()
                .map(r -> new SimpleGrantedAuthority(r.getRoleName()))
                .collect(java.util.stream.Collectors.toList());

        return new AppUserDto(
                first.getUsername(),
                first.getPassword(),
                authorities);
    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
