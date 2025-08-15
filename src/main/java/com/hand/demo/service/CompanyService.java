package com.hand.demo.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.security.auth.login.CredentialException;

import org.apache.tika.utils.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hand.demo.config.JwtService;
import com.hand.demo.model.Dtos.AppUserLoginDto;
import com.hand.demo.model.Dtos.AppUserRegisterDTO;
import com.hand.demo.model.Dtos.UpdateCompanyDto;
import com.hand.demo.model.Dtos.product_dtos.CreateProductDto;
import com.hand.demo.model.Dtos.product_dtos.ProductForCompany;
import com.hand.demo.model.entity.Address;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.Product;
import com.hand.demo.model.repository.AddressRepository;
import com.hand.demo.model.repository.AppUserRepository;
import com.hand.demo.model.repository.CompanyRepository;
import com.hand.demo.model.repository.GetReviewsProjection;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService extends AppUserService {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final JwtService jwtService;
    private final AppUserImageService appUserImageService;
    private final AddressRepository addressRepository;
    private final ProductService productService;
    private final ImageUrlService imageUrlService;

    // ################################
    // ###### Company Operations ######
    // ################################
    public Company findCompanyById(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not fund"));
    }

    public Company saveCompany(AppUserRegisterDTO registerDTO) throws IOException {
        if (StringUtils.isBlank(registerDTO.getUsername()) || StringUtils.isBlank(registerDTO.getPassword())) {
            throw new IllegalArgumentException("Please fill all fields");
        }
        if (appUserRepository.existsByUsername(registerDTO.getUsername())) {
            throw new IllegalArgumentException("Invalid Username");
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

    public AppUserLoginDto CreateCompany(AppUserRegisterDTO registerDTO, MultipartFile file) throws IOException {
        Company company = saveCompany(registerDTO);

        if (file != null) {
            addCompanyImg(company, file);
        }

        var jwtToken = jwtService.jwtGenerator(company);
        AppUserLoginDto appUserLoginDto = new AppUserLoginDto(company, jwtToken);
        return appUserLoginDto;
    }

    public Company updateCompanyHelper(UpdateCompanyDto companyDto) throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername()).get();
        company.updateDtoToAppUser(companyDto);
        return company;
    }

    public String updateCompany(UpdateCompanyDto companyDto) throws CredentialException {

        companyRepository.save(updateCompanyHelper(companyDto));
        return "User Update Successfully";
    }

    public Company updateCompany(UpdateCompanyDto companyDto, MultipartFile image)
            throws CredentialException, IOException {
        Company company = updateCompanyHelper(companyDto);
        if (image != null) {
            addCompanyImg(company, image);
        }
        return companyRepository.save(company);
    }

    public void addCompanyImg(Company company, MultipartFile file) throws IOException {
        if (company.getAppUserImage() == null) {
            appUserImageService.save(file, company);
        } else {
            appUserImageService.UpdateImageUrl(file, company.getAppUserImage());
        }
    }

    // ##################################
    // ######## Image Operations ########
    // ##################################
    public String addImg(MultipartFile file) throws IOException, CredentialException {
        super.userAuthorization();
        return imageUrlService.saveImage(file);
    }

    // ##################################
    // ####### Product Operations #######
    // ##################################
    public ProductForCompany createProductDto(CreateProductDto productDto) throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        return productService.createProduct(productDto, company);
    }

    public Product getCompanyProduct(Long productId) throws CredentialException {
    UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        return productService.getCompanyProductHelper(productId, company.getId());
    }

    public java.util.List<com.hand.demo.model.repository.CompanyProductProjection> listMyProducts()
            throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        return productService.getCompanyProductsForDisplay(company.getId());
    }

    public ProductForCompany updateMyProduct(CreateProductDto productDto, Long productId) throws CredentialException {
          UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
  
        return productService.updateProduct(productDto, productId,company.getId());
    }

    public void activateMyProduct(Long productId, boolean active) throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        productService.setActive(productId, company.getId(), active);
    }

    public String deleteMyProduct(Long productId) throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        productService.deleteCompanyProduct(productId, company.getId());
        return "Product deleted successfully";
    }

    public List<GetReviewsProjection> getReviewDesRat(Long productId) {

        return productService.getRatings(productId, Sort.by(Sort.Direction.DESC, "rating"));
    }
        public List<GetReviewsProjection> getReviewAscRat(Long productId) {

        return productService.getRatings(productId, Sort.by(Sort.Direction.ASC, "rating"));
    }
        public List<GetReviewsProjection> getReviewDesCreate(Long productId) {

        return productService.getRatings(productId, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

}
