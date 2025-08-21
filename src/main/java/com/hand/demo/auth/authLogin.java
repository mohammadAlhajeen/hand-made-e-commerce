/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.hand.demo.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.hand.demo.config.JwtService;
import com.hand.demo.model.Dtos.appuser_dtos.LogInRequest;

/**
 *
 * @author Mohammad
 */
@Service
public class AuthLogin {

        @Autowired
        private JwtService jwtService;
        @Autowired
        private AuthenticationManager authenticationManager;

        public AuthResponse loginAppUser(LogInRequest logInRequest) {
                Object userObject = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                logInRequest.getUsername(), logInRequest.getPassword()))
                                .getPrincipal();

                User user = userObject instanceof User ? (User) userObject : null;
                String jwtToken = jwtService.jwtGenerator(user);
                return AuthResponse.builder()
                                .token(jwtToken)
                                .build();
        }

}
