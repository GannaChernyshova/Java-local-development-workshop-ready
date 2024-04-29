package com.testcontainers.catalog;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class RunMe {

    public static void main(String[] args) {

        PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

        postgreSQLContainer.start();
        System.out.println("postres connection url " + postgreSQLContainer.getJdbcUrl() + " postgres user "
                + postgreSQLContainer.getUsername());
    }
}
