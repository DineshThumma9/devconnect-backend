package com.pm.jujutsu.model;


import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Project")
@Data
public class ProjectNode {

    @Id
    private String id;
    
    @org.springframework.data.annotation.Version
    private Long version;

    private String title;
    private String description;

    // Relationships managed manually via Neo4jService to avoid cascade version conflicts
    // Don't set these fields directly - use Neo4jService relationship methods instead
}
