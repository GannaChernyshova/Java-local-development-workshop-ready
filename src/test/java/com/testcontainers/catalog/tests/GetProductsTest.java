package com.testcontainers.catalog.tests;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.testcontainers.catalog.domain.ProductService;
import com.testcontainers.catalog.domain.models.CreateProductRequest;
import com.testcontainers.catalog.domain.models.Product;
import io.restassured.http.ContentType;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


class GetProductsTest extends BaseIntegrationTest {
    @Autowired
    ProductService productService;


    @Test
    void getProductByCodeSuccessfully() {
        productService.deleteAllProducts();
        productService.createProduct( new CreateProductRequest("P101", "Product P101", "Product P101 description", new BigDecimal("34.0")));
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
    void getProductByCodeFails() {
        String code = "P100000";

        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products/{code}", code)
                .then()
                .statusCode(404);
    }
}
