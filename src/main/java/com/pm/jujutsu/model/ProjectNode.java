package com.pm.jujutsu.model;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Project")
@Data
public class ProjectNode {

    @Id
    private ObjectId id;

    private String title;
    private String desc;

    @Relationship(type = "OWNED_BY", direction = Relationship.Direction.INCOMING)
    private UserNode owner;

    @Relationship(type = "CONTRIBUTING_TO", direction = Relationship.Direction.INCOMING)
    private Set<UserNode> currentContributors = new HashSet<>();

    @Relationship(type = "WORKS_WITH", direction = Relationship.Direction.OUTGOING)
    private Set<TagNode> tags = new HashSet<>();
}
