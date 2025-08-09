package com.hand.demo.model.repository;

public interface AppUserProjection {
    String getUsername();
    String getPassword();
    boolean isEnabled();
    boolean isAccountNonExpired();
    boolean isCredentialsNonExpired();
    boolean isAccountNonLocked();
    String getRoleName();
}
