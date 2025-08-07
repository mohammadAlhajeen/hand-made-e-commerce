package com.hand.demo.service;

import com.hand.demo.model.entity.AppUser;
import com.hand.demo.model.entity.ImageUrl;
import com.hand.demo.model.repository.ImageUrlRepository;
import java.beans.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import staticClasses.FileValidator;

@Service
public class ImageUrlService {

    private final Path storageLocation;
    @Autowired
    private ImageUrlRepository imageUrlRepository;

    public ImageUrlService() throws IOException {
        this.storageLocation = Paths.get("src/main/resources/static/images").toAbsolutePath().normalize();
        Files.createDirectories(this.storageLocation); // إنشاء المجلد إذا لم يكن موجودًا
    }

    public String saveImage(MultipartFile file) throws IOException {
        // التحقق من أن الملف ليس فارغًا
        if (file.isEmpty()) {
            throw new IOException("File Is Empty!");
        }

        // التحقق من نوع الملف الحقيقي
        FileValidator.validateImageFile(file);

        // تنظيف اسم الملف الأصلي وإنشاء اسم فريد باستخدام UUID
        String originalExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().substring(0, 5) + "_" + System.nanoTime() + "."
                + originalExtension;

        // تحديد المسار المستهدف للملف
        Path targetLocation = this.storageLocation.resolve(fileName);

        // نقل البيانات من InputStream إلى الملف بكفاءة
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }

        // حفظ الرابط في قاعدة البيانات
        return "/images/" + fileName;
    }

    public ImageUrl saveImage(AppUser user, MultipartFile file) throws IOException {
        ImageUrl imgUrl = new ImageUrl(saveImage(file), user);
        imageUrlRepository.save(imgUrl);
        return imgUrl;
    }

    public ImageUrl saveImageInDataBase(MultipartFile file) throws IOException {
        ImageUrl imgUrl = new ImageUrl(saveImage(file));
        imageUrlRepository.save(imgUrl);
        return imgUrl;
    }

    @Transient
    public ImageUrl UpdateImageUrl(MultipartFile file, ImageUrl url) throws IOException {

        Path path = Paths.get("src/main/resources/static/" + url.getUrl());
        String newUrl = saveImage(file);
        url.setUrl(newUrl);
        ImageUrl imgUrl = imageUrlRepository.save(url);
        Files.deleteIfExists(path);
        return imgUrl;
    }

}
