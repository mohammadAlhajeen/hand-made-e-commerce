package com.hand.demo.storage;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStaticResourcesConfig implements WebMvcConfigurer {

    @Value("${app.images.storage-dir}") String storageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String loc = Paths.get(storageDir).toAbsolutePath().normalize().toString();
        if (!loc.endsWith("/") && !loc.endsWith("\\")) loc = loc + "/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + loc)
                .setCachePeriod(31536000); // 1 سنة (اختياري)
    }
}
