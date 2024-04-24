package com.testcontainers.catalog.tests;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.testcontainers.catalog.ContainersConfig;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        //We have configured the RestAssured.port to the dynamic port of the application that is started by Spring Boot.
        webEnvironment = RANDOM_PORT,
        //We have configured the spring.kafka.consumer.auto-offset-reset property to earliest to make sure that we read all the messages from the beginning of the topic.
        properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
//We have reused the ContainersConfig class that we created to define all the required containers.
@Import(ContainersConfig.class)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpBase() {
        RestAssured.port = port;
    }
}
