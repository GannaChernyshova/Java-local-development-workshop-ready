package com.testcontainers.catalog.domain;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public static ProductNotFoundException withCode(String code) {
        return new ProductNotFoundException("Product with the " + code + " is not found");
    }
}
