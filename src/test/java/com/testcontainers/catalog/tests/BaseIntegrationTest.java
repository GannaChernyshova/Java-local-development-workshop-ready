package com.testcontainers.catalog.tests;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.testcontainers.catalog.ContainersConfig;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
@Import(ContainersConfig.class)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpBase() {
        RestAssured.port = port;
    }
}
