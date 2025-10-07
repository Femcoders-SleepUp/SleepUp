package com.SleepUp.SU.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@Profile("test")  // Activate only in test profile
public class TestMailConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSender mockMailSender = Mockito.mock(JavaMailSender.class);

        // Create a dummy MimeMessage to return when createMimeMessage is called
        Mockito.when(mockMailSender.createMimeMessage())
                .thenReturn(new MimeMessage((Session) null));

        // Prevent sending emails actually
        Mockito.doNothing().when(mockMailSender).send(Mockito.any(MimeMessage.class));

        return mockMailSender;
    }
}