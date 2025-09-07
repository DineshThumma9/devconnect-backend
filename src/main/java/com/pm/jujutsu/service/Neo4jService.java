package com.pm.jujutsu.service;

import com.pm.jujutsu.dtos.ProjectResponseDTO;
import jakarta.servlet.http.HttpSession;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.neo4j.driver.Values.parameters;

@Service
public class Neo4jService {

    @Autowired
    private Driver driver;


    @Autowired
    private Neo4jClient neo4jClient;





    private List<String> extractSingleColumn(Result result, String columnName) {
        List<String> list = new ArrayList<>();
        while (result.hasNext()) {
            list.add(result.next().get(columnName).asString());
        }
        return list;
    }

    // ---------------- User Tag Sync ----------------
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

    // ---------------- Post Recommendations ----------------
    public List<String> recommendPostBasedOnTags(String userId, List<String> tags) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (u:User {id: $uid})-[:INTERESTED_IN]->(t:Tag)<-[:TAGGED_WITH]-(p:Post)
                    WHERE t.name IN $tags
                    RETURN DISTINCT p.id AS postId
                    ORDER BY p.timestamp DESC
                    LIMIT 20
                """, parameters("uid", userId, "tags", tags));

                return extractSingleColumn(result, "postId");
            });
        }
    }

    public List<String> recommendPostBasedOnConnectionsAndTags(String userId, List<String> tags) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (:User {id: $uid})-[:FOLLOWS]->(f:User)-[:TAGGED_WITH]->(p:Post)<-[:TAGGED_WITH]-(t:Tag)
                    WHERE t.name IN $tags
                    RETURN DISTINCT p.id AS postId
                    ORDER BY p.timestamp DESC
                    LIMIT 20
                """, parameters("uid", userId, "tags", tags));

                return extractSingleColumn(result, "postId");
            });
        }
    }

    public Map<String, Object> getPostById(String postId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                MATCH (p:Post {id: $postId})
                RETURN p
            """, parameters("postId", postId));

                if (result.hasNext()) {
                    return result.next().get("p").asNode().asMap();
                } else {
                    return null;  // Post not found
                }
            });
        }
    }

    public Map<String, Object> getProjectById(String projectId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                MATCH (p:Project {id: $projectId})
                RETURN p
            """, parameters("postId", projectId));

                if (result.hasNext()) {
                    return result.next().get("p").asNode().asMap();
                } else {
                    return null;  // Post not found
                }
            });
        }
    }

    public Map<String, Object> getUserById(String userId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                MATCH (p:user {id: $userId})
                RETURN p
            """, parameters("postId", userId));

                if (result.hasNext()) {
                    return result.next().get("p").asNode().asMap();
                } else {
                    return null;  // Post not found
                }
            });
        }
    }

    // ---------------- Project Recommendations ----------------
    public List<String> getProjectBasedOnInterests(String userId, List<String> tags) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (u:User {id: $uid})-[:INTERESTED_IN]->(t:Tag)<-[:WORKS_WITH]-(p:Project)
                    WHERE t.name IN $tags
                    RETURN DISTINCT p.id AS projectId
                    ORDER BY p.timestamp DESC
                    LIMIT 20
                """, parameters("uid", userId, "tags", tags));

                return extractSingleColumn(result, "projectId");
            });
        }
    }

    public List<String> recommendProjectBasedOnConnectionsAndTags(String userId, List<String> tags) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (:User {id: $uid})-[:FOLLOWS]->(f:User)-[:WORKS_WITH]->(p:Project)<-[:WORKS_WITH]-(t:Tag)
                    WHERE t.name IN $tags
                    RETURN DISTINCT p.id AS projectId
                    ORDER BY p.timestamp DESC
                    LIMIT 20
                """, parameters("uid", userId, "tags", tags));

                return extractSingleColumn(result, "projectId");
            });
        }
    }

    public List<String> getProjectBasedOnDescTags(String userId, List<String> tags, String projectDesc) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (pr:Project)-[:WORKS_WITH]->(t:Tag)
                    WHERE t.name IN $tags AND toLower(pr.desc) CONTAINS toLower($desc)
                    RETURN DISTINCT pr.id AS projectId
                    ORDER BY pr.timestamp DESC
                    LIMIT 20
                """, parameters("tags", tags, "desc", projectDesc));

                return extractSingleColumn(result, "projectId");
            });
        }
    }

    // ---------------- User Connections ----------------
    public List<String> getConnectionBasedOnInterest(List<String> tags) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (u:User)-[:INTERESTED_IN]->(t:Tag)
                    WHERE t.name IN $tags
                    RETURN DISTINCT u.id AS userId
                    LIMIT 20
                """, parameters("tags", tags));

                return extractSingleColumn(result, "userId");
            });
        }
    }


    public void syncPostTags(String postId, Set<String> tags) {
        String cypher = """
        UNWIND $tags AS tagName
        MERGE (t:Tag {name: tagName})
        WITH t
        MATCH (p:Post {id: $postId})
        MERGE (p)-[:TAGGED_WITH]->(t)
    """;

        neo4jClient.query(cypher)
                .bindAll(Map.of("postId", postId, "tags", tags))
                .run();
    }


    public void syncProjectTags(String projectId, Set<String> tags) {
        String cypher = """
        UNWIND $tags AS tagName
        MERGE (t:Tag {name: tagName})
        WITH t
        MATCH (pr:Project {id: $projectId})
        MERGE (pr)-[:WORKS_WITH]->(t)
    """;

        neo4jClient.query(cypher)
                .bindAll(Map.of("projectId", projectId, "tags", tags))
                .run();
    }



    public List<String> getConnectionsBasedOnConnectionsAndInterests(String userId, List<String> tags) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                    MATCH (:User {id: $uid})-[:FOLLOWS]->(f:User)-[:INTERESTED_IN]->(t:Tag)
                    WHERE t.name IN $tags
                    RETURN DISTINCT f.id AS userId
                    LIMIT 20
                """, parameters("uid", userId, "tags", tags));

                return extractSingleColumn(result, "userId");
            });
        }
    }





    public void createLikeRelationship(String userId, String postId) {
        String cypher = """
            MATCH (u:User {id: $userId}), (p:Post {id: $postId})
            MERGE (u)-[:LIKED]->(p)
        """;

        neo4jClient.query(cypher)
                .bindAll(Map.of("userId", userId, "postId", postId))
                .run();
    }


    public void removeLikeRelationship(String userId, String postId) {
        String query = """
        MATCH (u:User {id: $userId})-[l:LIKED]->(p:Post {id: $postId})
        DELETE l
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("postId", postId);

        neo4jClient.query(query).bindAll(params).run();
    }



    public void followRelationship(String userId, String followId) {
        String cypher = """
            MATCH (u:User {id: $userId}), (p:User {id: $userId})
            MERGE (u)-[:FOLLOWS]->(p)
        """;

        neo4jClient.query(cypher)
                .bindAll(Map.of("userId", userId, "followId", followId))
                .run();
    }


    public void unfollowRelationship(String userId, String followId) {
        String cypher = """
             MATCH (u:User {id: $userId})-[l:FOLLOWS]->(p:User {id: $userId})
             DELETE l
        """;

        neo4jClient.query(cypher)
                .bindAll(Map.of("userId", userId, "followId", followId))
                .run();
    }



    public void createSubscibeRelationship(String userId, String projectId) {
        String cypher = """
            MATCH (u:User {id: $userId}), (p:Project {id: $projectId})
            MERGE (u)-[:SUBSCRIBE]->(p)
        """;

        neo4jClient.query(cypher)
                .bindAll(Map.of("userId", userId, "projectId", projectId))
                .run();
    }


    public void removeSubscribeRelationship(String userId, String projectId) {
        String cypher = """
          MATCH (u:User {id: $userId})-[l:SUBSCRIBED]->(p:Project {id: $projectId})
           DELETE l
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("projectId", projectId);

        neo4jClient.query(cypher).bindAll(params).run();
    }








}
