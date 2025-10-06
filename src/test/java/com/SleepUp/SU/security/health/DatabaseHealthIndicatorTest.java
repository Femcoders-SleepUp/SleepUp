package com.SleepUp.SU.security.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseHealthIndicatorTest {

    private DataSource dataSource;
    private Connection connection;
    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    public void setup() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        healthIndicator = new DatabaseHealthIndicator(dataSource);
    }

    @Test
    public void health_connectionIsValid_shouldReturnStatusUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        Health health = healthIndicator.health();

        assertEquals(Health.up().withDetail("database", "UP").build().getStatus(), health.getStatus());
        assertEquals("UP", health.getDetails().get("database"));
        verify(connection).close();
    }

    @Test
    public void health_connectionIsInvalid_shouldReturnStatusDown() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);

        Health health = healthIndicator.health();

        assertEquals(Health.down().withDetail("database", "DOWN").build().getStatus(), health.getStatus());
        assertEquals("DOWN", health.getDetails().get("database"));
        verify(connection).close();
    }

    @Test
    public void health_getConnectionThrowsException_shouldReturnStatusDown() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("DB error"));

        Health health = healthIndicator.health();

        assertEquals(Health.down().build().getStatus(), health.getStatus());
        assertEquals("DOWN", health.getDetails().get("database"));
    }
}
