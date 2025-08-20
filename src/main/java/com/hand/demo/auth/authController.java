package com.hand.demo.auth;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hand.demo.model.Dtos.AppUserRegisterDTO;
import com.hand.demo.model.Dtos.LogInRequest;
import com.hand.demo.service.CompanyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/authcontroller")
@RequiredArgsConstructor
public class AuthController {

    private final CompanyService companyService;
    private final AuthLogin authLog;

    @PostMapping("register")
    public ResponseEntity<?> companyRegister(@Valid @RequestBody AppUserRegisterDTO registerDTO) throws IOException {

        return new ResponseEntity<>(companyService.createCompany(registerDTO), HttpStatus.OK);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LogInRequest login) {

        return new ResponseEntity<>(authLog.loginAppUser(login), HttpStatus.OK);

    }
}