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
import java.math.BigDecimal;
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
    void shouldUploadProductImageSuccessfully() throws IOException {
        String code = "P101";
        File file = new ClassPathResource("P101.jpg").getFile();

        Optional<Product> product = productService.getProductByCode(code);
        assertThat(product).isPresent();
        assertThat(product.get().imageUrl()).isNull();

        given().multiPart("file", file, "multipart/form-data")
                .contentType(ContentType.MULTIPART)
                .when()
                .post("/api/products/{code}/image", code)
                .then()
                .statusCode(200)
                .body("status", endsWith("success"))
                .body("filename", endsWith("P101.jpg"));

        await().pollInterval(Duration.ofSeconds(3)).atMost(10, SECONDS).untilAsserted(() -> {
            Optional<Product> optionalProduct = productService.getProductByCode(code);
            assertThat(optionalProduct).isPresent();
            assertThat(optionalProduct.get().imageUrl()).isNotEmpty();
        });
    }

    @Test
    void getProductByCodeSuccessfully() {
        String code = "P101";

        Product product = given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/{code}", code)
                .then()
                .statusCode(200)
                .extract()
                .as(Product.class);

        assertThat(product.code()).isEqualTo(code);
        assertThat(product.name()).isEqualTo("Product %s".formatted(code));
        assertThat(product.description()).isEqualTo("Product %s description".formatted(code));
        assertThat(product.price().compareTo(new BigDecimal("34.0"))).isEqualTo(0);
        assertThat(product.available()).isTrue();
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
                .statusCode(400);
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

    @Test
    void failsToGetProductByCodeIfCodeExists() {
        String code = UUID.randomUUID().toString();
        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/{code}", code)
                .then()
                .statusCode(404);
    }
}