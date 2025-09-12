package com.pm.jujutsu.repository;

import com.pm.jujutsu.model.PostNode;
import org.bson.types.ObjectId;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Repository
public interface PostNodeRespository extends Neo4jRepository<PostNode, ObjectId> {


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
            MERGE (p)-[TAGGED_WITH]->(t)
            """)
    void syncPostTags(ObjectId postId, Set<String> tags);


    @Query("""
               
                           MATCH (u:User {id: $userId})-[:INTERESTED_IN]->(t:Tag)<-[:TAGGED_WITH]-(p:Post)
                           WHERE t.name IN $tags
                           RETURN DISTINCT p.id AS postId
                           ORDER BY p.timestamp DESC
                           LIMIT 20
            """)
    List<ObjectId> recommendPostBasedOnUserInterests(ObjectId userId, Set<String> tags);




    @Query("""
            
            
            MATCH (:User {id: $userId})-[:FOLLOWS]->(f:User)-[:TAGGED_WITH]->(p:Post)<-[:TAGGED_WITH]-(t:Tag)
            WHERE t.name IN $tags
            RETURN DISTINCT p.id AS postId
            ORDER BY p.timestamp DESC
            LIMIT 20
          
          """)
    List<ObjectId> recommendPostBasedOnUserFollowsAndInterests(String userId,Set<String> tags);









}
