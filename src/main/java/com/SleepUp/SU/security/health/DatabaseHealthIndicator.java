package com.SleepUp.SU.security.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(5);
            if (valid) {
                return Health.up().withDetail("database", "UP").build();
            } else {
                return Health.down().withDetail("database", "DOWN").build();
            }
        } catch (SQLException e) {
            return Health.down().withDetail("database", "DOWN").withException(e).build();
        }
    }
}