package com.SleepUp.SU.config;

import com.SleepUp.SU.config.properties.AppProperties;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final AppProperties appProperties;

    @Bean
    public Cloudinary cloudinary() {
        AppProperties.CloudinaryProperties cp = appProperties.getCloudinary();
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cp.getCloudName(),
                "api_key", cp.getApiKey(),
                "api_secret", cp.getApiSecret()
        ));
    }
}
