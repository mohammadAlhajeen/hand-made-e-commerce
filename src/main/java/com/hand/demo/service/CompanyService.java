package com.hand.demo.service;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.CredentialException;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.hand.demo.model.Dtos.appuser_dtos.AppUserLoginDto;
import com.hand.demo.model.Dtos.appuser_dtos.AppUserRegisterDTO;
import com.hand.demo.model.Dtos.appuser_dtos.GetUpdatedAppUserDto;
import com.hand.demo.model.Dtos.appuser_dtos.UpdateAppUserDto;
import com.hand.demo.model.Dtos.product_dtos.CreateInStockProductDto;
import com.hand.demo.model.Dtos.product_dtos.CreatePreOrderProductDto;
import com.hand.demo.model.Dtos.product_dtos.InStockProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.PreOrderProductForCompanyV1;
import com.hand.demo.model.Dtos.product_dtos.ProductDTOs;
import com.hand.demo.model.Dtos.product_dtos.UpdateInStockProductDto;
import com.hand.demo.model.Dtos.product_dtos.UpdatePreOrderProductDto;
import com.hand.demo.model.entity.Company;
import com.hand.demo.repository.CompanyRepository;
import com.hand.demo.repository.GetReviewsProjection;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService extends AppUserService {
    private final CompanyRepository companyRepository;
    private final ProductService productService;
    private final InStockProductService inStockProductService;
    private final PreOrderProductService preOrderProductService;

    // ################################
    // ###### Company Operations ######
    // ################################
    public Company findCompanyById(Long id) {
    return companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public Company saveAppUserHelper(AppUserRegisterDTO registerDTO) throws IOException {
        Company company = (Company) super.saveAppUserHelper(registerDTO);
        companyRepository.save(company);
        return company;
    }

    public AppUserLoginDto createCompany(AppUserRegisterDTO registerDTO) throws IOException {
        // Delegate to AppUserService implementation which handles creation + JWT
        return super.createAppUser(registerDTO);
    }

    public Company updateCompanyHelper(UpdateAppUserDto companyDto) throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername()).get();
        company.updateDtoToAppUser(companyDto);

        return company;
    }

    public GetUpdatedAppUserDto updateCompany(UpdateAppUserDto companyDto) throws CredentialException {

        Company company = updateCompanyHelper(companyDto);
        if (companyDto.getMediaId() != null) {
            appUserImageService.setAvatar(company, companyDto.getMediaId());
        }
        GetUpdatedAppUserDto updateAppUserDto = new GetUpdatedAppUserDto(companyRepository.save(company));
        return updateAppUserDto;
    }


    // ##################################
    // ####### Product Operations #######
    // ##################################
    public PreOrderProductForCompanyV1 createPreOrderProductDto(CreatePreOrderProductDto productDto)
            throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        return preOrderProductService.createOrderProduct(productDto, company);
    }

    public InStockProductForCompanyV1 createInStockProductDto(CreateInStockProductDto productDto)
            throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));

        return inStockProductService.createInStockProductDto(productDto, company);
    }

    public ProductDTOs getCompanyProduct(Long productId) throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        return productService.getCompanyProduct(productId, company.getId());
    }

    public java.util.List<com.hand.demo.repository.CompanyProductProjection> listMyProducts()
            throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        return productService.getCompanyProductsForDisplay(company.getId());
    }

    public PreOrderProductForCompanyV1 updateMyProduct(UpdatePreOrderProductDto dto, Long productId)
            throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));

        return preOrderProductService.updateProduct(dto, productId, company.getId());
    }

    public InStockProductForCompanyV1 updateMyProduct(UpdateInStockProductDto dto, Long productId)
            throws CredentialException {
        UserDetails appUser = super.userAuthorization();
        Company company = companyRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));

        return inStockProductService.updateProduct(dto, productId, company.getId());
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
