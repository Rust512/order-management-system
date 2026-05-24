package com.design.order_management_system.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.postgresql.PostgreSQLContainer;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(DatabaseCleanupExtension.class)
@ExtendWith(DatabaseCleanupExtension.class)
public abstract class DatabaseTest {
    @ServiceConnection
    protected static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest");

    static {
        postgreSQLContainer.start();
    }
}
