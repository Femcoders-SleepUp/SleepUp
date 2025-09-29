package com.SleepUp.SU.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring")
@Validated
public class AppProperties {

    @Valid
    private DatasourceProperties datasource = new DatasourceProperties();

    @NotNull(message = "server.port must not be null")
    private Integer serverPort = 8080;

    @Valid
    private MailProperties mail = new MailProperties();

    @Valid
    private JwtProperties jwt = new JwtProperties();

    @NotBlank(message = "spring.application.name must not be blank")
    private String applicationName = "SleepUpApp";

    @Valid
    private CloudinaryProperties cloudinary = new CloudinaryProperties();

    @Data
    @Validated
    public static class DatasourceProperties {
        @NotBlank(message = "spring.datasource.url must not be blank")
        private String url = "jdbc:mysql://localhost:3306/testdb";

        @NotBlank(message = "spring.datasource.username must not be blank")
        private String username = "root";

        @NotBlank(message = "spring.datasource.password must not be blank")
        private String password = "password";
    }

    @Data
    @Validated
    public static class MailProperties {
        @NotBlank(message = "spring.mail.username must not be blank")
        private String username = "example@gmail.com";

        @NotBlank(message = "spring.mail.password must not be blank")
        private String password = "secret";

        @NotBlank(message = "spring.mail.from must not be blank")
        private String from = "example@gmail.com";
    }

    @Data
    @Validated
    public static class JwtProperties {
        @NotBlank(message = "jwt.secret must not be blank")
        private String secret = "default_jwt_secret";

        @NotNull(message = "jwt.expiration-ms must not be null")
        private Long expirationMs = 1800000L;

        @NotNull(message = "jwt.refresh-expiration-ms must not be null")
        private Long refreshExpirationMs = 604800000L;
    }

    @Data
    @Validated
    public static class CloudinaryProperties {
        @NotBlank(message = "cloudinary.cloud_name must not be blank")
        private String cloudName = "test_cloud";

        @NotBlank(message = "cloudinary.api_key must not be blank")
        private String apiKey = "test_key";

        @NotBlank(message = "cloudinary.api_secret must not be blank")
        private String apiSecret = "test_secret";
    }
}
