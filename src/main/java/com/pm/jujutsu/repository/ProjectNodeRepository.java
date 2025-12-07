package com.pm.jujutsu.repository;


import com.pm.jujutsu.model.ProjectNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProjectNodeRepository extends Neo4jRepository<ProjectNode, String> {




    @Query(" MATCH (u:User {id: $userId}), (p:Project {id: $projectId}) MERGE (u)-[:SUBSCRIBE]->(p)")
    void subscribeRelation(String userId, String projectId);



    @Query("MATCH (u:User {id:$userId) - [l:SUBSCRIBED_TO] -> (p:Project {id:$projectId} DELETE l")
    void unsubscribeRelation(String userId, String projectId);


    @Query("""
          UNWIND $tags AS tagName
           MERGE (t:Tag {name: tagName})
           WITH t
           MATCH (p:Project {id: $projectId})
            MERGE (p)-[:WORK_WITH]->(t)
            
            """)
    void syncProjectTags(
            String projectId,
            Set<String> tags

    );



    @Query("""
              MATCH (u:User {id: $userId})-[:INTERESTED_IN]->(t:Tag)<-[:WORKS_WITH]-(p:Project)
                           WHERE t.name IN $tags
                           RETURN DISTINCT p.id AS projectId
                           LIMIT 20
            """)
    List<String> recommendProjectBasedOnUserInterests(String userId, Set<String> tags);




    @Query("""
            
            
            MATCH (:User {id: $userId})-[:FOLLOWS]->(f:User)-[:TAGGED_WITH]->(p:Project)<-[:WORKS_WITH]-(t:Tag)
            WHERE t.name IN $tags
            RETURN DISTINCT p.id AS postId
            LIMIT 20
          
          """)
    List<String>  recommendProjectBasedOnUserFollowsAndInterests(String userId,Set<String> tags);









}
