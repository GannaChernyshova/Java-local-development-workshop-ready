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

// Let's create src/test/resources/test-data.sql to insert some test data into the database before tests
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

        //   Check that before uploading the image, the product image URL is null for the product with code P101.
        Optional<Product> product = productService.getProductByCode(code);
        assertThat(product).isPresent();
        assertThat(product.get().imageUrl()).isNull();

        //  Invoke the Product Image Upload API endpoint with the sample image file.
        //  Assert that the response status is 200 and the response body contains the image file name.
        given().multiPart("file", file, "multipart/form-data")
                .contentType(ContentType.MULTIPART)
                .when()
                .post("/api/products/{code}/image", code)
                .then()
                .statusCode(200)
                .body("status", endsWith("success"))
                .body("filename", endsWith("P101.jpg"));

        //  Assert that the product image URL is updated in the database after the image upload.
        await().pollInterval(Duration.ofSeconds(3)).atMost(10, SECONDS).untilAsserted(() -> {
            Optional<Product> optionalProduct = productService.getProductByCode(code);
            assertThat(optionalProduct).isPresent();
            assertThat(optionalProduct.get().imageUrl()).isNotEmpty();
        });
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
