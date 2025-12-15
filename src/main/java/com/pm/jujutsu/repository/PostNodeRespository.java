package com.pm.jujutsu.repository;

import com.pm.jujutsu.model.PostNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Repository
public interface PostNodeRespository extends Neo4jRepository<PostNode, String> {


    @Query("MATCH (u:User {id:$userId}) (p:Post {id:$postId} MERGE (p) <- [:LIKED_BY] - (u)")
    void likeRelationship(String userId, String postId);


    @Query("""
            MATCH (u:User {id: $userId})-[l:LIKED]->(p:Post {id: $postId})
            DELETE l
        """)
    void  dislikeRelationship(String userId, String postId);



    @Query("""
            UNWIND $tags AS tagName
            MERGE (t:Tag {name: tagName})
            WITH t
            MATCH (p:Post {id: $postId})
            MERGE (p)-[:TAGGED_WITH]->(t)
            """)
    void syncPostTags(String postId, Set<String> tags);


    @Query("""
               
                           MATCH (u:User {id: $userId})-[:INTERESTED_IN]->(t:Tag)<-[:TAGGED_WITH]-(p:Post)
                           WHERE t.name IN $tags
                           WITH DISTINCT p
                           RETURN p.id AS postId
                           ORDER BY p.timestamp DESC
                           LIMIT 20
            """)
    List<String> recommendPostBasedOnUserInterests(String userId, Set<String> tags);




    @Query("""
            
            
            MATCH (:User {id: $userId})-[:FOLLOWS]->(f:User)-[:TAGGED_WITH]->(p:Post)<-[:TAGGED_WITH]-(t:Tag)
            WHERE t.name IN $tags
            WITH DISTINCT p
            RETURN p.id AS postId
            ORDER BY p.timestamp DESC
            LIMIT 20
          
          """)
    List<String> recommendPostBasedOnUserFollowsAndInterests(String userId,Set<String> tags);









}
