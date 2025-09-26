package com.SleepUp.SU.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.bind.annotation.RestController;

@Configuration
public class ApiVersioningTestConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("api/v1",
                HandlerTypePredicate.forAnnotation(RestController.class));
    }
}
