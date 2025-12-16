package com.pm.jujutsu.config;

import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;

@Configuration
public class TransactionConfig {

    /**
     * Mark Neo4j transaction manager as primary to resolve the conflict
     * between MongoDB's reactive transaction manager and Neo4j's transaction manager.
     */
    @Primary
    @Bean(name = "transactionManager")
    public Neo4jTransactionManager transactionManager(Driver driver, DatabaseSelectionProvider databaseSelectionProvider) {
        return new Neo4jTransactionManager(driver, databaseSelectionProvider);
    }
}
