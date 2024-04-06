package com.testcontainers.catalog.tests;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.endsWith;

import com.testcontainers.catalog.domain.ProductService;
import com.testcontainers.catalog.domain.models.Product;
import io.restassured.http.ContentType;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.jdbc.Sql;

@Sql("/test-data.sql")
class ProductControllerTest extends com.testcontainers.catalog.tests.BaseIntegrationTest {
    @Autowired
    ProductService productService;

    @Test
    void createProductSuccessfully() {
        String code = UUID.randomUUID().toString();
        given().contentType(ContentType.JSON)
                .body(
                        """
                    {
                        "code": "%s",
                        "name": "Product %s",
                        "description": "Product %s description",
                        "price": 10.0
                    }
                    """
                                .formatted(code, code, code))
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .header("Location", endsWith("/api/products/%s".formatted(code)));
    }

    @Test
    void failsToCreateProductIfPayloadInvalid() {
        String code = UUID.randomUUID().toString();
        given().contentType(ContentType.JSON)
                .body(
                        """
                    {
                        "code": "%s",
                        "description": "Product %s description",
                        "price": 10.0
                    }
                    """
                                .formatted(code, code, code))
                .when()
                .post("/api/products")
                .then()
                .statusCode(400)
                .body("detail", endsWith("Invalid request content."));
    }

    @Test
    void failsToCreateProductIfProductCodeExists() {
        String code = UUID.randomUUID().toString();
        given().contentType(ContentType.JSON)
                .body(
                        """
                    {
                        "code": "%s",
                        "name": "Product %s",
                        "description": "Product %s description",
                        "price": 10.0
                    }
                    """
                                .formatted(code, code, code))
                .when()
                .post("/api/products")
                .then()
                .statusCode(201);

        given().contentType(ContentType.JSON)
                .body(
                        """
                    {
                        "code": "%s",
                        "name": "Another Product %s",
                        "description": "Another product %s description",
                        "price": 11.0
                    }
                    """
                                .formatted(code, code, code))
                .when()
                .post("/api/products")
                .then()
                .statusCode(500);
    }
}
