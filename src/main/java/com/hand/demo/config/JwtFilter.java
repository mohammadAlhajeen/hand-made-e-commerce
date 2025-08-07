/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hand.demo.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hand.demo.service.AppUserService;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Mohammad
 */
@Component
@RequiredArgsConstructor // as autowired because Object is final
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserService appUserService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String filterHeader = request.getHeader("Authorization");

        final String jwtToken;
        final String username;
        if (filterHeader == null || !filterHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwtToken = filterHeader.substring(7);
        username = jwtService.getUsernameFromToken(jwtToken);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = appUserService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwtToken, user)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }

        }
        filterChain.doFilter(request, response);
    }

    
}
