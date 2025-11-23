package com.amit.mymarket.it;

import com.amit.mymarket.it.config.PostgreSqlContainer;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

@DataJpaTest
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@ImportTestcontainers(PostgreSqlContainer.class)
public abstract class AbstractRepositoryIT {
}
