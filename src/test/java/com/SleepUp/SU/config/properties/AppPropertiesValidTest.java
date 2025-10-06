package com.SleepUp.SU.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableConfigurationProperties(AppProperties.class)
@TestPropertySource(properties = {
        "jwt.secret=testSecret",
        "jwt.expiration-ms=60000",
        "jwt.refresh-expiration-ms=120000",
        "cloudinary.cloud-name=testCloudName",
        "cloudinary.api-key=testApiKey",
        "cloudinary.api-secret=testApiSecret"
})
class AppPropertiesValidTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    void propertiesShouldBindCorrectly() {
        assertThat(appProperties).isNotNull();
        assertThat(appProperties.getJwt().getSecret()).isEqualTo("testSecret");
    }

    @Configuration
    @EnableConfigurationProperties(AppProperties.class)
    static class TestConfig {
    }
}

