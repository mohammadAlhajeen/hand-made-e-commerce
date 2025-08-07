package com.hand.model.Dtos;

import java.util.Set;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompanyDto {
    private String username;
    private String name;
    private String phone;
    private Set<Long> addressId;
    private String urlLocation;
}
