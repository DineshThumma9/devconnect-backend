package com.pm.jujutsu.model;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Node("User")
@Data
public class UserNode {

    @Id
    private String id;

    @Relationship(type = "INTERESTED_IN", direction = Relationship.Direction.OUTGOING)
    private Set<TagNode> interestedIn = new HashSet<>();

    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private Set<UserNode> follows = new HashSet<>();

    @Relationship(type = "FOLLOWED_BY", direction = Relationship.Direction.INCOMING)
    private Set<UserNode> followedBy = new HashSet<>();

    @Relationship(type = "SUBSCRIBED_TO", direction = Relationship.Direction.OUTGOING)
    private Set<ProjectNode> subscribedProjects = new HashSet<>();
}
