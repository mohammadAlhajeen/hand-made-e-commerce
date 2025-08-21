package com.hand.demo.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.entity.AppUser;
import com.hand.demo.model.entity.AppUserImage;
import com.hand.demo.model.entity.MediaItem;
import com.hand.demo.repository.AppUserImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserAvatarService {

  private final AppUserImageRepository appUserImageRepo;
  private final MediaService mediaService;

  @Transactional
  public AppUserImage setAvatar(AppUser user, UUID image) {
    MediaItem mediaItem = mediaService.findMediaById(image);
    AppUserImage appUserImage = AppUserImage.builder()
        .mediaItem(mediaItem)
        .appUser(user)
        .build();
    user.setAppUserImage(appUserImage);

    return appUserImageRepo.save(appUserImage);
  }


  @Transactional
  public void removeAvatar(AppUser appUser) {
    appUserImageRepo.findByAppUser(appUser).ifPresent(appUserImageRepo::delete);
  }
}
