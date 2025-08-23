package com.hand.demo.service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
import org.springframework.web.multipart.MultipartFile;

import com.hand.demo.config.JwtService;
import com.hand.demo.model.Dtos.appuser_dtos.AppUserDto;
import com.hand.demo.model.Dtos.appuser_dtos.AppUserLoginDto;
import com.hand.demo.model.Dtos.appuser_dtos.AppUserRegisterDTO;
import com.hand.demo.model.entity.Address;
import com.hand.demo.model.entity.AppUser;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.MediaItem;
import com.hand.demo.repository.AddressRepository;
import com.hand.demo.repository.AppUserProjection;
import com.hand.demo.repository.AppUserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AppUserService implements UserDetailsService {
    @Autowired
    protected AppUserRepository appUserRepository;
    @Autowired
    protected JwtService jwtService;
    @Autowired
    protected AppUserAvatarService appUserImageService;
    @Autowired
    protected AddressRepository addressRepository;
    @Autowired
    protected MediaService mediaService;

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

    // ################################
    // ###### Company Operations ######
    // ################################
    public AppUser saveAppUserHelper(AppUserRegisterDTO registerDTO) throws IOException {

        if (appUserRepository.existsByUsername(registerDTO.getUsername())) {
            throw new IllegalArgumentException("Invalid Username");
        }

        registerDTO.setPassword(passwordEncoder().encode(registerDTO.getPassword()));
        AppUser appUser = new Company();
        appUser.RegisterDtoToAppUser(registerDTO);
        if (Objects.nonNull(registerDTO.getAddressId())) {
            Set<Address> addressSet = new HashSet<>(addressRepository.findAllById(registerDTO.getAddressId()));
            appUser.setAddress(addressSet);
        }

        return appUser;
    }

    public List<MediaItem> findMediaByUserId(Long userId) {
        return mediaService.findMediaByUserId(userId);
    }

    public AppUserLoginDto createAppUser(AppUserRegisterDTO registerDTO) throws IOException {
        AppUser appUser = saveAppUserHelper(registerDTO);
        var jwtToken = jwtService.jwtGenerator(appUser);
        AppUserLoginDto appUserLoginDto = new AppUserLoginDto(appUser, jwtToken);
        return appUserLoginDto;
    }


    // ##################################
    // ######## Image Operations ########
    // ##################################
    public MediaItem addImg(MultipartFile file) throws IOException, CredentialException {
        UserDetails appUser = this.userAuthorization();
        AppUser fappUser = appUserRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));

        return mediaService.uploadImage(fappUser.getId(), file);
    }

    public void removeImg(UUID imageId) throws IOException, CredentialException {
        UserDetails appUser = this.userAuthorization();
        AppUser fappUser = appUserRepository.findByUsername(appUser.getUsername())
                .orElseThrow(() -> new CredentialException("Company not found"));
        mediaService.removeItem(imageId, fappUser.getId());

    }

}
