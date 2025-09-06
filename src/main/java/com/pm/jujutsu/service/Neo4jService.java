package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.ProjectResponseDTO;
import org.bson.types.ObjectId;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

@Service
public class Neo4jService {

    @Autowired
    public Driver driver;

    public void syncUserTags(String userId, List<String> tags) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                for (String tag : tags) {
                    tx.run("""
                        MERGE (t:Tag {name: $tag})
                        WITH t
                        MATCH (u:User {id: $uid})
                        MERGE (u)-[:INTERESTED_IN]->(t)
                    """, parameters("tag", tag, "uid", userId));
                }
                return null;
            });
        }
    }

    public List<String> recommendPostBasedOnTags(String userId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (u:User {id: $uid})-[:INTERESTED_IN]->(t:Tag)<-[:HAS_TAG]-(p:Post)
                    RETURN DISTINCT p.id AS postId
                    ORDER BY p.timestamp DESC
                    LIMIT 20
                """, parameters("uid", userId));

                List<String> postIds = new ArrayList<>();
                while (result.hasNext()) {
                    postIds.add(result.next().get("postId").asString());
                }
                return postIds;
            });
        }
    }

    public List<String> getProjectBasedOnInterests(String userId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (u:User {id: $uid})-[:INTERESTED_IN]->(t:Tag)<-[:HAS_TAG]-(pr:Project)
                    RETURN DISTINCT pr.id AS projectId
                    ORDER BY pr.timestamp DESC
                    LIMIT 10
                """, parameters("uid", userId));

                List<String> projectIds = new ArrayList<>();
                while (result.hasNext()) {
                    projectIds.add(result.next().get("projectId").asString());
                }
                return projectIds;
            });
        }
    }

    public List<String> getConnectionBasedOnInterest(String userId) {
        return null;
    }


    public List<ProjectResponseDTO> getProjectBasedOnConnectionsAndInterests(ObjectId id, List<String> interests) {
        return null;
    }


    public List<ProjectResponseDTO> getProjectBasedOnDescTags(
            String userId,
            List<String> tags,
            String projectDesc
    ){


    }
 }
