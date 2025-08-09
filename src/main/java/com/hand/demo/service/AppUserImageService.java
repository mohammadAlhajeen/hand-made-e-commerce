package com.hand.demo.service;

import com.hand.demo.model.entity.AppUser;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.demo.model.entity.AppUserImage;
import com.hand.demo.model.repository.AppUserImageRepository;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AppUserImageService extends ImageUrlService {
    @Autowired
    private AppUserImageRepository appUserImageRepository;

    public AppUserImageService() throws IOException {
        super();
    }


    public List<AppUserImage> findAll() {
        return appUserImageRepository.findAll();
    }

    public Optional<AppUserImage> findById(Long id) {
        return appUserImageRepository.findById(id);
    }

    public AppUserImage save(MultipartFile file, AppUser appUser) throws IOException {
        AppUserImage appImg = new AppUserImage();
        appImg.setAppUser(appUser);
        appImg.setUrl(super.saveImage(file));
        return appUserImageRepository.save(appImg);
    }

    public void deleteById(Long id) {
        appUserImageRepository.deleteById(id);
    }
}
