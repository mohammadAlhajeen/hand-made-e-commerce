package com.hand.model.repository;

public interface AppUserProjection {
    String getUsername();
    String getPassword();
    boolean getEnabled();
    boolean getAccountNonExpired();
    boolean getCredentialsNonExpired();
    boolean getAccountNonLocked();
    String getRoleName(); // كل صف يحتوي Role واحد
}
