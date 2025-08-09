package com.hand.demo.auth;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hand.demo.model.Dtos.AppUserRegisterDTO;
import com.hand.demo.model.Dtos.LogInRequest;
import com.hand.demo.service.CompanyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/authcontroller")
@RequiredArgsConstructor
public class authController {

    private final CompanyService companyService;
    private final authLogin authLog;

    @PostMapping("register")
    public ResponseEntity<?> companyRegister(@ModelAttribute AppUserRegisterDTO registerDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

    
            return new ResponseEntity<>(companyService.CreateCompany(registerDTO, image), HttpStatus.OK);


    }

    @PostMapping("login")
    public ResponseEntity<?> Login(@RequestBody LogInRequest login) {

        return new ResponseEntity<>(authLog.LogInAppUser(login), HttpStatus.OK);

    }
}