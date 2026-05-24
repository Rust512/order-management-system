package com.design.order_management_system.config;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

@AutoConfigureTestDatabase(replace = Replace.NONE)
public abstract class DatabaseTest {
    @ServiceConnection
    protected static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest");

    static {
        postgreSQLContainer.start();
    }
}
