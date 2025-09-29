package com.SleepUp.SU.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Validated
@Getter
@Setter
public class DatasourceProperties {

    @NotBlank(message = "spring.datasource.url must not be blank")
    private String url;

    @NotBlank(message = "spring.datasource.username must not be blank")
    private String username;

    @NotBlank(message = "spring.datasource.password must not be blank")
    private String password;
}