package com.pm.jujutsu.repository;


import com.pm.jujutsu.model.UserNode;
import org.bson.types.ObjectId;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserNodeRepository  extends Neo4jRepository<UserNode, ObjectId>{


    @Query("MATCH (u:User {id:$userId) (f:User {id:$fId} " +
            "MERGE (u)-[:FOLLOWS]-(f)")
    void followRelationship(String userId, String fId);



    @Query("""
            MATCH (u:User {id: $userId})-[l:FOLLOWS]->(p:User {id: $followId})
            DELETE l
        """)
    void  unfollowRelationship(String userId, String fId);



    @Query("""
            UNWIND $tags AS tagName
            MERGE (t:Tag {name: tagName})
            WITH t
            MATCH (u:User {id: $userId})
            MERGE (u)-[:INTERESTED_IN]->(t)
            
            """)
    void syncUserTags(
            ObjectId userId,
            Set<String> tags
    );


    @Query("""
               
            MATCH (u:User)-[:INTERESTED_IN]->(t:Tag)
            WHERE t.name IN $tags
            RETURN DISTINCT u.id AS userId
            LIMIT 20
            """)
    List<String> recommendConnectionsBasedOnUserInterests(String userId, Set<String> tags);




    @Query("""
            
            
            MATCH (:User {id: $userId})-[:FOLLOWS]->(f:User)-[:INTERESTED_IN]->(t:Tag)
            WHERE t.name IN $tags
            RETURN DISTINCT f.id AS userId
            LIMIT 20
          
          """)
    List<String>  recommendConnectionsBasedOnUserFollowsAndInterests(String userId,Set<String> tags);










}
