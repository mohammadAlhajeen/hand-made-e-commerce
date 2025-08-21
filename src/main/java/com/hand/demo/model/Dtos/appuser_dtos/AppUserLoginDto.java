package com.hand.demo.model.Dtos.appuser_dtos;

import com.hand.demo.model.Dtos.image_dtos.GetImageDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AppUserLoginDto {

    private Long id;
    private String name;
    private String phone;
    private String username;
    private GetImageDto appUserImage;
    private String token;

    public AppUserLoginDto(com.hand.demo.model.entity.AppUser appUser, String token) {
        this.id = appUser.getId();
        this.name = appUser.getName();
        this.phone = appUser.getPhone();
        this.username = appUser.getUsername();
        if (appUser.getAppUserImage() != null) {
            this.appUserImage = new GetImageDto(appUser.getAppUserImage().getMediaItem().getId(), appUser.getAppUserImage().getMediaItem().getAbsoluteUrl());
        }

        this.token = token;
    }
}
