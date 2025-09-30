package com.SleepUp.SU.config;

import com.SleepUp.SU.config.properties.DatasourceProperties;
import com.cloudinary.Cloudinary;
import com.cloudinary.Api;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.ApplicationArguments;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StartupValidationRunnerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private DatasourceProperties datasourceProperties;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Api cloudinaryApi;

    @Mock
    private Connection connection;

    private StartupValidationRunner runner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        runner = new StartupValidationRunner(dataSource, datasourceProperties, cloudinary);
    }

    @Nested
    class RunTests {

        @Test
        void run_withValidConnections_shouldCompleteSuccessfully() throws Exception {
            when(datasourceProperties.getUrl()).thenReturn("jdbc:testdb");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isClosed()).thenReturn(false);
            when(cloudinary.api()).thenReturn(cloudinaryApi);
            doReturn(null).when(cloudinaryApi).usage(anyMap());

            runner.run(mock(ApplicationArguments.class));

            verify(dataSource).getConnection();
            verify(cloudinary).api();
            verify(cloudinaryApi).usage(anyMap());
        }

        @Test
        void run_withCloudinaryFailure_shouldThrowRuntimeException() throws Exception {
            when(datasourceProperties.getUrl()).thenReturn("jdbc:testdb");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isClosed()).thenReturn(false);
            when(cloudinary.api()).thenReturn(cloudinaryApi);
            doThrow(new RuntimeException("Cloudinary down")).when(cloudinaryApi).usage(anyMap());

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    runner.run(mock(ApplicationArguments.class))
            );

            assertTrue(exception.getMessage().contains("Cloudinary connection validation failed"));
        }
    }

    @Nested
    class ValidateDatabaseConnectionTests {

        @Test
        void validateDatabaseConnection_withNullUrl_shouldThrowRuntimeException() {
            when(datasourceProperties.getUrl()).thenReturn(null);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                runner.run(mock(ApplicationArguments.class));
            });

            assertEquals("DB URL is invalid or missing", exception.getMessage());
        }

        @Test
        void validateDatabaseConnection_withEmptyUrl_shouldThrowRuntimeException() {
            when(datasourceProperties.getUrl()).thenReturn("");

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                runner.run(mock(ApplicationArguments.class));
            });

            assertEquals("DB URL is invalid or missing", exception.getMessage());
        }

        @Test
        void validateDatabaseConnection_withNullConnection_shouldThrowRuntimeException() throws SQLException {
            when(datasourceProperties.getUrl()).thenReturn("jdbc:testdb");
            when(dataSource.getConnection()).thenReturn(null);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                runner.run(mock(ApplicationArguments.class));
            });

            assertEquals("Database connection not available", exception.getMessage());
        }

        @Test
        void validateDatabaseConnection_withClosedConnection_shouldThrowRuntimeException() throws SQLException {
            when(datasourceProperties.getUrl()).thenReturn("jdbc:testdb");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.isClosed()).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                runner.run(mock(ApplicationArguments.class));
            });

            assertEquals("Database connection not available", exception.getMessage());
            verify(connection).close();
        }

        @Test
        void validateDatabaseConnection_withSQLException_shouldThrowRuntimeException() throws SQLException {
            when(datasourceProperties.getUrl()).thenReturn("jdbc:testdb");
            when(dataSource.getConnection()).thenThrow(new SQLException("DB down"));

            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                runner.run(mock(ApplicationArguments.class));
            });

            assertTrue(exception.getMessage().contains("Failed to connect to DB"));
            assertTrue(exception.getCause() instanceof SQLException);
        }
    }
}
