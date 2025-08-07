package com.hand.demo.model.Dtos;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class AppUserDto extends User {

    public AppUserDto(String username, String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        //TODO Auto-generated constructor stub
    }

}
