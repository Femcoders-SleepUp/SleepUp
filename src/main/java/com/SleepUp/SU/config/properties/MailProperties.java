package com.SleepUp.SU.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "spring.mail")
@Validated
@Getter
@Setter
public class MailProperties {

    @NotBlank(message = "spring.mail.username must not be blank")
    private String username;

    @NotBlank(message = "spring.mail.password must not be blank")
    private String password;

    @NotBlank(message = "spring.mail.from must not be blank")
    private String from;

    @NotNull(message = "spring.mail.port must not be null")
    private Integer port;
}
