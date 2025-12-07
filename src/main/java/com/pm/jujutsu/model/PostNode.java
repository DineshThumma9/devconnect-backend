package com.pm.jujutsu.model;


import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Post")
@Data
public class PostNode {

    @Id
    private String id;

    @Relationship(type = "LIKED_BY", direction = Relationship.Direction.INCOMING)
    private Set<UserNode> likedBy = new HashSet<>();

    @Relationship(type = "SHARED_BY", direction = Relationship.Direction.INCOMING)
    private Set<UserNode> sharedBy = new HashSet<>();

    @Relationship(type = "TAGGED_WITH", direction = Relationship.Direction.OUTGOING)
    private Set<TagNode> tags = new HashSet<>();
}
