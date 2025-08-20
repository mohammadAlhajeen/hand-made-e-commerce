package com.hand.demo.model.Dtos;

import java.util.Set;
import java.util.UUID;

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
    private String name;
    private String phone;
    private Set<Long> addressId;
    private String urlLocation;
    private UUID mediaId;
}
