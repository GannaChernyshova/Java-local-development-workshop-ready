package com.testcontainers.catalog;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class RunMe {
    public static void main(String[] args) {
        MongoDBContainer mongoDBContainer =
                new MongoDBContainer(DockerImageName.parse("mongo:7.0.7-jammy"));

        mongoDBContainer.start();
        System.out.println(mongoDBContainer.getConnectionString());
    }
}
