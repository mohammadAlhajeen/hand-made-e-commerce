/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.hand.auth;

import com.hand.config.JwtService;
import com.hand.model.Dtos.LogInRequest;
import com.hand.model.repository.AppUserRepository;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;


import org.springframework.security.core.userdetails.User;

/**
 *
 * @author Mohammad
 */
@Service
public class authLogin {

        @Autowired
        private AppUserRepository appUserRepository;
        @Autowired
        private JwtService jwtService;
        @Autowired
        private AuthenticationManager authenticationManager;

        public AuthResponse LogInAppUser(LogInRequest logInRequest) throws IOException {
                Object userObject = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                logInRequest.getUsername(), logInRequest.getPassword()))
                                .getPrincipal();
// appUserRepository.findByUsername(logInRequest.getUsername()).orElseThrow(()
                // -> new UsernameNotFoundException("user Not found"));
                
                User user = userObject instanceof User ? (User) userObject : null;
                String jwtToken = jwtService.jwtGenerator(user);
                return AuthResponse.builder()
                                .token(jwtToken)
                                .build();
        }

}
