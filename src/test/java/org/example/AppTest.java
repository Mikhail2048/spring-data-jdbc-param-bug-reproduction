package org.example;

import java.time.OffsetDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import junit.framework.TestCase;
import junit.framework.TestSuite;

@SpringBootTest
public class AppTest {

    @Autowired
    private SomeEntityRepository someEntityRepository;

    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.2"))
      .withDatabaseName("local")
      .withUsername("postgres")
      .withPassword("postgres")
      .withInitScript("init.sql");

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgreSQLContainer.stop();
    }

    @DynamicPropertySource
    static void register(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> String.format("jdbc:postgresql://localhost:%d/local", postgreSQLContainer.getFirstMappedPort()));
    }

    @Test //That test is failing
    void onAppReady() {
        someEntityRepository.deleteByCreatedAt(4);
    }
}

class SomeEntity {

    @Id
    private Long id;

    private String name;

    private OffsetDateTime createdAt;
}

//language=sql
interface SomeEntityRepository extends CrudRepository<SomeEntity, Long> {

    @Query("DELETE FROM some_entity WHERE created_at + INTERVAL ':hoursAgo hours' < NOW()") // here comes the problem
    void deleteByCreatedAt(@Param("hoursAgo") int hoursAgo);
}