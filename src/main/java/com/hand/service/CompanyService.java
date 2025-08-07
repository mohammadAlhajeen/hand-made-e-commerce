package com.hand.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hand.auth.AuthResponse;
import com.hand.config.JwtService;
import com.hand.model.Dtos.AppUserRegisterDTO;
import com.hand.model.Dtos.UpdateCompanyDto;

import com.hand.model.entity.Address;
import com.hand.model.entity.Company;

import com.hand.model.repository.AddressRepository;
import com.hand.model.repository.AppUserRepository;
import com.hand.model.repository.CompanyRepository;

import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService extends AppUserService {


    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final JwtService jwtService;
    private final ImageUrlService imageUrlService;
  
    private final AddressRepository addressRepository;

    public Company findCompanyById(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not fund"));
    }

    public Company saveCompany(AppUserRegisterDTO registerDTO) throws IOException {
        if (StringUtils.isBlank(registerDTO.getUsername()) || StringUtils.isBlank(registerDTO.getPassword())) {
            throw new RuntimeException("Please fill all fields");
        }
        if (appUserRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("Invalid Username");
        }
        registerDTO.setPassword(passwordEncoder().encode(registerDTO.getPassword()));
        Company company = new Company();
        company.RegisterDtoToAppUser(registerDTO);
        if (Objects.nonNull(registerDTO.getAddressId())) {
            Set<Address> addressSet = new HashSet<>(addressRepository.findAllById(registerDTO.getAddressId()));
            company.setAddress(addressSet);
        }
        companyRepository.save(company);
        return company;
    }

    public AuthResponse CreateCompany(AppUserRegisterDTO registerDTO, MultipartFile file) throws IOException {
        Company company = saveCompany(registerDTO);

        if (file != null) {
            addCompanyImg(company, file);
        }

        var jwtToken = jwtService.jwtGenerator(company);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public Company updateCompanyHelper(UpdateCompanyDto companyDto) {
        super.userAuthorization();
        Company company = companyRepository.findByUsername(companyDto.getUsername()).get();
        company.updateDtoToAppUser(companyDto);
        return company;
    }

    public String updateCompany(UpdateCompanyDto companyDto) {

        companyRepository.save(updateCompanyHelper(companyDto));
        return "User Update Successfully";
    }

    public void addCompanyImg(Company company, MultipartFile file) throws IOException {
        if (company.getUrlImgs() == null) {
            imageUrlService.saveImage(company, file);
        } else {
            imageUrlService.UpdateImageUrl(file, company.getUrlImgs());
        }
    }






}
