package com.amit.mymarket.it.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public final class PostgreSqlContainer {

    private static final DockerImageName IMAGE_NAME = DockerImageName.parse("postgres:17.6-alpine3.22");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(IMAGE_NAME);

}
