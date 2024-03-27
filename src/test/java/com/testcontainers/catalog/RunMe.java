package com.testcontainers.catalog;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class RunMe {
    public static void main(String[] args) {
        PostgreSQLContainer postgreSQLContainer =
                new PostgreSQLContainer(DockerImageName.parse("postgres:16.2-alpine"));

        postgreSQLContainer.start();
        String ps = postgreSQLContainer.getJdbcUrl();
        System.out.println("Postges is running:" + ps);
    }
}
