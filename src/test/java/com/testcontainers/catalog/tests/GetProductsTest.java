package com.testcontainers.catalog.tests;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;

import com.testcontainers.catalog.domain.ProductService;
import com.testcontainers.catalog.domain.models.Product;
import io.restassured.http.ContentType;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.jdbc.Sql;

@Sql("/test-data.sql")
class GetProductsTest extends BaseIntegrationTest {
    @Autowired
    ProductService productService;

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
    void getProductByWrongCode() {
        String code = "P100001";

        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/{code}", code)
                .then()
                .statusCode(404)
                .body("detail", equalTo(String.format("%s is not found", code)));
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
}
