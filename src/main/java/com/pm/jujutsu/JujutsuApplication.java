package com.pm.jujutsu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableMongoAuditing
@EnableMongoRepositories
@EnableNeo4jRepositories
public class JujutsuApplication {

    public static void main(String[] args) {
        SpringApplication.run(JujutsuApplication.class, args);
    }

}

