package com.testcontainers.catalog;

import static org.testcontainers.utility.DockerImageName.parse;

import com.testcontainers.catalog.domain.FileStorageService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

// annotation indicates that this configuration class defines the beans that can be used for Spring Boot tests.
@TestConfiguration(proxyBeanMethods = false)
public class ContainersConfig {
    @Bean
    // Spring Boot provides `ServiceConnection` support for `JdbcConnectionDetails` out-of-the-box.
    // This configuration will automatically start container and register the **DataSource** connection properties
    // automatically.
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        //  .withReuse:
        //  When you first start the application, the containers will be created.
        //  When you stop the application, the containers will continue to run.
        //  When you restart the application again, the containers will be reused.
        return new PostgreSQLContainer<>(parse("postgres:16-alpine")).withReuse(false);
    }

    @Bean
    // Spring Boot provides `ServiceConnection` support for `KafkaConnectionDetails` out-of-the-box
    // This configuration will automatically start container and register **Kafka** connection properties automatically.
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(parse("confluentinc/cp-kafka:7.5.3"));
    }

    @Bean("localstackContainer")
        //  Spring Cloud AWS doesn't provide ServiceConnection support out-of-the-box.
        //  So we configured `LocalStackContainer` as a bean and registered the Spring Cloud AWS configuration properties using `DynamicPropertyRegistry`
    LocalStackContainer localstackContainer(DynamicPropertyRegistry registry) {
        LocalStackContainer localStack = new LocalStackContainer(parse("localstack/localstack:2.3"));
        registry.add("spring.cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localStack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localStack::getRegion);
        registry.add("spring.cloud.aws.endpoint", localStack::getEndpoint);
        return localStack;
    }

    @Bean
    //    We also configured an `ApplicationRunner` bean to create the AWS resources like S3 bucket upon application
    // startup.
    //    It'll wait for the `localstackContainer` to be started first
    @DependsOn("localstackContainer")
    ApplicationRunner awsInitializer(ApplicationProperties properties, FileStorageService fileStorageService) {
        return args -> fileStorageService.createBucket(properties.productImagesBucketName());
    }

    @Bean
        //    We'll start WireMockContainer using `DynamicPropertyRegistry` and register the Wiremock server URL as `application.inventory-service-url`.
        //    So, when we make a call to inventory-service from our application, it will call the Wiremock server endpoint instead and get responses predefined in the mocks-config.json
    WireMockContainer wiremockServer(DynamicPropertyRegistry registry) {
        WireMockContainer wiremockServer =
                new WireMockContainer("wiremock/wiremock:3.2.0-alpine").withMappingFromResource("mocks-config.json");
        registry.add("application.inventory-service-url", wiremockServer::getBaseUrl);
        return wiremockServer;
    }
}
