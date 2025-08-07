package com.hand.model.Dtos;

import java.util.Set;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class AppUserRegisterDTO {

    private String username;
    private String password;
    private boolean deleted ;
    private String name;
   private String urlLocation;
    private String phone;
    private Set <Long> addressId;
}
