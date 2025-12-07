package com.pm.jujutsu.utils;


import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig{


    @Value("${neo4j.uri:bolt://localhost:7687}")
    private String url;

    @Value("${neo4j.username:neo4j}")
    private String username;


    @Value("${neo4j.password:password}")
    private String password;



    @Bean
    public Driver neo4Driver(){
        return GraphDatabase.driver(url,  AuthTokens.basic(username,password));
    }



}
