package com.SleepUp.SU.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties
@Validated
public class AppProperties {

    @Valid
    private JwtProperties jwt = new JwtProperties();

    @Valid
    private CloudinaryProperties cloudinary = new CloudinaryProperties();

    @Data
    public static class JwtProperties {
        @NotBlank(message = "jwt.secret must not be blank")
        private String secret;

        @NotNull(message = "jwt.expiration-ms must not be null")
        private Long expirationMs;

        @NotNull(message = "jwt.refresh-expiration-ms must not be null")
        private Long refreshExpirationMs;
    }

    @Data
    public static class CloudinaryProperties {
        @NotBlank(message = "cloudinary.cloud_name must not be blank")
        private String cloudName;

        @NotBlank(message = "cloudinary.api_key must not be blank")
        private String apiKey;

        @NotBlank(message = "cloudinary.api_secret must not be blank")
        private String apiSecret;
    }
}