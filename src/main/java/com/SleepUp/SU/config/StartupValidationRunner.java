package com.SleepUp.SU.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class StartupValidationRunner implements ApplicationRunner {

    private final AppProperties appProperties;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        validateConnections();
    }

    private void validateConnections() {
        validateDatabaseConnection();
//        validateCloudinaryConnection();
    }

    private void validateDatabaseConnection() {
        if (appProperties.getDatasource() == null
                || appProperties.getDatasource().getUrl() == null
                || appProperties.getDatasource().getUrl().isEmpty()) {
            throw new RuntimeException("DB URL is invalid or missing");
        }
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null || connection.isClosed()) {
                throw new RuntimeException("Database connection not available");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to DB: " + e.getMessage(), e);
        }
    }

//    private void validateCloudinaryConnection() {
//        AppProperties.CloudinaryProperties cloudinaryProps = appProperties.getCloudinary();
//
//        String cloudName = cloudinaryProps.getCloudName();
//        String apiKey = cloudinaryProps.getApiKey();
//        String apiSecret = cloudinaryProps.getApiSecret();
//
//        if (cloudName == null || apiKey == null || apiSecret == null
//                || cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
//            throw new RuntimeException("Cloudinary credentials are missing or blank.");
//        }
//
//        Cloudinary cloudinaryInstance = new Cloudinary(ObjectUtils.asMap(
//                "cloud_name", cloudName,
//                "api_key", apiKey,
//                "api_secret", apiSecret
//        ));
//        try {
//            cloudinaryInstance.api().usage(ObjectUtils.emptyMap());
//        } catch (Exception e) {
//            throw new RuntimeException("Cloudinary connection validation failed: " + e.getMessage(), e);
//        }
//    }

}
