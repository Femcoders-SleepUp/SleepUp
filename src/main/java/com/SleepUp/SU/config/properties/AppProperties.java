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
@ConfigurationProperties(prefix = "")
@Validated
public class AppProperties {

//    @Valid
//    private DatasourceProperties datasource = new DatasourceProperties();
//
//    @Valid
//    private MailProperties mail = new MailProperties();

    @Valid
    private JwtProperties jwt = new JwtProperties();

    @Valid
    private CloudinaryProperties cloudinary = new CloudinaryProperties();

//    @Data
//    public static class DatasourceProperties {
//        @NotBlank(message = "spring.datasource.url must not be blank")
//        private String url;
//
//        @NotBlank(message = "spring.datasource.username must not be blank")
//        private String username;
//
//        @NotBlank(message = "spring.datasource.password must not be blank")
//        private String password;
//    }
//
//    @Data
//    public static class MailProperties {
//        @NotBlank(message = "spring.mail.username must not be blank")
//        private String username;
//
//        @NotBlank(message = "spring.mail.password must not be blank")
//        private String password;
//
//        @NotBlank(message = "spring.mail.from must not be blank")
//        private String from;
//
//        @NotNull(message = "spring.mail.port must not be null")
//        private Integer port;
//    }


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
