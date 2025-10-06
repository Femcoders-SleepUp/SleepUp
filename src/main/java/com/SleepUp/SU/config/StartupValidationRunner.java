package com.SleepUp.SU.config;

import com.SleepUp.SU.config.properties.DatasourceProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.cloudinary.Cloudinary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class StartupValidationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupValidationRunner.class);

    private final DataSource dataSource;
    private final DatasourceProperties datasourceProperties;

    @Autowired
    private final Cloudinary cloudinary;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("StartupValidationRunner started running");
        validateConnections();
        logger.info("StartupValidationRunner completed validations successfully");
    }

    private void validateConnections() {
        validateDatabaseConnection();
        validateCloudinaryConnection();
    }

    private void validateDatabaseConnection() {
        logger.info("Validating database connection");

        if (datasourceProperties.getUrl() == null
                || datasourceProperties.getUrl().isEmpty()) {
            logger.warn("DB URL is invalid or missing");
            throw new RuntimeException("DB URL is invalid or missing");
        }

        try (Connection connection = dataSource.getConnection()) {
            if (connection == null || connection.isClosed()) {
                logger.warn("Database connection not available");
                throw new RuntimeException("Database connection not available");
            } else {
                logger.info("Database connection is valid");
            }
        } catch (SQLException e) {
            logger.warn("Failed to connect to DB: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to DB: " + e.getMessage(), e);
        }
    }

    public void validateCloudinaryConnection() {
        logger.info("Validating Cloudinary connection");
        try {
            cloudinary.api().usage(com.cloudinary.utils.ObjectUtils.emptyMap());
            logger.info("Cloudinary connection is valid");
        } catch (Exception e) {
            logger.warn("Cloudinary connection validation failed: {}", e.getMessage());
            throw new RuntimeException("Cloudinary connection validation failed: " + e.getMessage(), e);
        }
    }
}
