/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hand.demo.model.entity;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hand.demo.model.Dtos.appuser_dtos.AppUserRegisterDTO;
import com.hand.demo.model.Dtos.appuser_dtos.UpdateAppUserDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Mohammad
 */
@Entity

@SQLDelete(sql = "UPDATE App_Users SET deleted = true WHERE id = ?")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@Setter
@Getter
@NoArgsConstructor
@Table(name = "App_Users")
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String username;

    @NotEmpty
    @Column(nullable = false)
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();
    @Column(nullable = false)
    private String name;

    private String phone;

    @OneToOne( fetch = FetchType.LAZY)
      @JsonBackReference
private AppUserImage appUserImage;

    @Column(nullable = false)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
     @JsonBackReference
 private Set<Role> roles;
    @Column(nullable = false)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "app_user_address", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "address_id"))
      @JsonBackReference
private Set<Address> address;

    private boolean deleted = false;

    private String urlLocation;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

 





    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.deleted = false;
    }

    public void updateDtoToAppUser(UpdateAppUserDto updateAppUserDto) {
        this.setName(Optional.ofNullable(updateAppUserDto.getName()).orElse(this.getName()));
        this.setPhone(Optional.ofNullable(updateAppUserDto.getPhone()).orElse(this.getPhone()));
        this.setUrlLocation(Optional.ofNullable(updateAppUserDto.getUrlLocation()).orElse(this.getUrlLocation()));

    }

    public void RegisterDtoToAppUser(AppUserRegisterDTO appUserRegisterDTO) {
        this.setName(appUserRegisterDTO.getName());
        this.setPassword(appUserRegisterDTO.getPassword());
        this.setPhone(appUserRegisterDTO.getPhone());
        this.setUrlLocation(appUserRegisterDTO.getUrlLocation());
        this.setUsername(appUserRegisterDTO.getUsername());
        this.setDeleted(false);
    }

}
