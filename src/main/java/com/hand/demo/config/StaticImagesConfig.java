package com.hand.demo.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.NonNull;

@Configuration
public class StaticImagesConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
    @Value("${app.images.storage-dir}")
     String storageDir;
    @Override
    public void addResourceHandlers(@NonNull org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        String loc = Paths.get(storageDir).toAbsolutePath().normalize().toString();
        if (!loc.endsWith("/") && !loc.endsWith("\\")) loc = loc + "/";
        registry.addResourceHandler("/images/**").addResourceLocations("file:" + loc).setCachePeriod(31536000);
    }
}
