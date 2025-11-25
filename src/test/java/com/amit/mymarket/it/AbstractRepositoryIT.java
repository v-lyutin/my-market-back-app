package com.amit.mymarket.it;

import com.amit.mymarket.it.config.PostgreSqlContainer;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@DataR2dbcTest
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@ImportTestcontainers(PostgreSqlContainer.class)
public abstract class AbstractRepositoryIT {
}
