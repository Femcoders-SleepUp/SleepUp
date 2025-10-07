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
class AppPropertiesCoverageTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    void propertiesShouldBindAndGettersShouldBeCovered() {
        assertThat(appProperties).isNotNull();

        assertThat(appProperties.getJwt()).isNotNull();
        assertThat(appProperties.getJwt().getSecret()).isEqualTo("testSecret");
        assertThat(appProperties.getJwt().getExpirationMs()).isEqualTo(60000L);
        assertThat(appProperties.getJwt().getRefreshExpirationMs()).isEqualTo(120000L);

        assertThat(appProperties.getCloudinary()).isNotNull();
        assertThat(appProperties.getCloudinary().getCloudName()).isEqualTo("testCloudName");
        assertThat(appProperties.getCloudinary().getApiKey()).isEqualTo("testApiKey");
        assertThat(appProperties.getCloudinary().getApiSecret()).isEqualTo("testApiSecret");
    }

    @Test
    void shouldInvokeSettersAndGettersExplicitly() {
        AppProperties props = new AppProperties();

        // Explicitly call setters
        AppProperties.JwtProperties jwt = new AppProperties.JwtProperties();
        jwt.setSecret("testSecret");
        jwt.setExpirationMs(60000L);
        jwt.setRefreshExpirationMs(120000L);

        AppProperties.CloudinaryProperties cloud = new AppProperties.CloudinaryProperties();
        cloud.setCloudName("testCloudName");
        cloud.setApiKey("testApiKey");
        cloud.setApiSecret("testApiSecret");

        props.setJwt(jwt);
        props.setCloudinary(cloud);

        // Explicitly call getters
        assertThat(props.getJwt().getSecret()).isEqualTo("testSecret");
        assertThat(props.getJwt().getExpirationMs()).isEqualTo(60000L);
        assertThat(props.getJwt().getRefreshExpirationMs()).isEqualTo(120000L);

        assertThat(props.getCloudinary().getCloudName()).isEqualTo("testCloudName");
        assertThat(props.getCloudinary().getApiKey()).isEqualTo("testApiKey");
        assertThat(props.getCloudinary().getApiSecret()).isEqualTo("testApiSecret");
    }


    @Configuration
    @EnableConfigurationProperties(AppProperties.class)
    static class TestConfig {
    }
}
