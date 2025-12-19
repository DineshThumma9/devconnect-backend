package com.pm.jujutsu.service;

import com.pm.jujutsu.model.PostNode;
import com.pm.jujutsu.model.ProjectNode;
import com.pm.jujutsu.repository.PostNodeRespository;
import com.pm.jujutsu.repository.ProjectNodeRepository;
import com.pm.jujutsu.repository.UserNodeRepository;
import com.pm.jujutsu.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import com.pm.jujutsu.model.UserNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class Neo4jService {



    @Autowired
    private UserNodeRepository userNodeRepository;

    @Autowired
    private ProjectNodeRepository projectNodeRepository;

    @Autowired
    private PostNodeRespository postNodeRespository;

    @Autowired
    private Neo4jClient neo4jClient;

    @Autowired
    private JwtUtil jwtUtil;


    // ---------------- User Node Management ----------------
    public void createUserNode(String userId) {
        UserNode userNode = new UserNode();
        userNode.setId(userId);
        userNodeRepository.save(userNode);
    }

    public void syncUserTags(String userId, Set<String> tags) {
        userNodeRepository.syncUserTags(userId, tags);
    }

    public Optional<UserNode> getUserById(String userId){
        return  userNodeRepository.findById(userId);
    }

    public void syncPostTags(String postId, Set<String> tags) {
        postNodeRespository.syncPostTags(postId, tags);
    }

    public void syncProjectTags(String projectId, Set<String> tags) {
        projectNodeRepository.syncProjectTags(projectId, tags);
    }


    public List<String> recommendPostBasedOnTags(String userId, Set<String> tags) {
        return postNodeRespository.recommendPostBasedOnUserInterests(userId, tags);
    }

    public List<String> recommendPostBasedOnConnectionsAndTags(String userId, Set<String> tags) {
        return postNodeRespository.recommendPostBasedOnUserFollowsAndInterests(userId, tags);

    }

    // ---------------- Project Recommendations ----------------
    public List<String> getProjectBasedOnInterests(String userId, Set<String> tags) {
        return projectNodeRepository.recommendProjectBasedOnUserInterests(userId, tags);
    }

    public List<String> recommendProjectBasedOnConnectionsAndTags(String userId, Set<String> tags) {
        return projectNodeRepository.recommendProjectBasedOnUserFollowsAndInterests(userId, tags);
    }


    // ---------------- User Connections ----------------
    public List<String> getConnectionsBasedOnInterests(Set<String> tags) {
        return userNodeRepository.recommendConnectionsBasedOnUserInterests(String.valueOf(jwtUtil.getCurrentUser().getId()), tags);
    }

    public List<String> getConnectionsBasedOnConnectionsAndInterests(String userId, Set<String> tags) {
        return userNodeRepository.recommendConnectionsBasedOnUserInterests(userId, tags);

    }


    public void createLikeRelationship(String userId, String postId) {
        postNodeRespository.likeRelationship(userId, postId);
    }

    public void removeLikeRelationship(String userId, String postId) {

        postNodeRespository.dislikeRelationship(userId, postId);
    }

    public void followRelationship(String userId, String followId) {
        userNodeRepository.followRelationship(userId, followId);
    }

    public void unfollowRelationship(String userId, String followId) {
        userNodeRepository.unfollowRelationship(userId, followId);
    }

    public void subscribeToProject(String userId, String projectId) {
        projectNodeRepository.subscribeRelation(userId, projectId);
    }

    public void unsubscribeFromProject(String userId, String projectId) {
        projectNodeRepository.unsubscribeRelation(userId, projectId);
    }

    public Optional<PostNode> getPostById(String postId) {
        return postNodeRespository.findById(postId);
    }

    // ---------------- Node Creation (without relationships) ----------------
    
    public void createProjectNode(String projectId, String title, String description) {
        ProjectNode projectNode = new ProjectNode();
        projectNode.setId(projectId);
        projectNode.setTitle(title);
        projectNode.setDescription(description);
        projectNodeRepository.save(projectNode);
    }
    
    public void createPostNode(String postId) {
        PostNode postNode = new PostNode();
        postNode.setId(postId);
        postNodeRespository.save(postNode);
    }

    // ---------------- Relationship Creation (manual, no cascade) ----------------
    
    /**
     * Creates OWNED_BY relationship: (Project)<-[:OWNED_BY]-(User)
     */
    public void createProjectOwnerRelationship(String projectId, String userId) {
        neo4jClient.query(
            "MATCH (p:Project {id: $projectId}) " +
            "MATCH (u:User {id: $userId}) " +
            "MERGE (p)<-[:OWNED_BY]-(u)"
        )
        .bindAll(Map.of("projectId", projectId, "userId", userId))
        .run();
    }


    public void createUserInterestsRelationship(String userId,Set<String> interests){
        neo4jClient.query(
            "MATCH (u:User {id: $userId}) " +
            "UNWIND $interests AS tagName " +
            "MERGE (t:Tag {name: tagName}) " +
            "MERGE (u)-[:INTERESTED_IN]->(t)"
        )
        .bindAll(Map.of("userId", userId, "interests", interests))
        .run();
    }
    

    /**
     * Creates CONTRIBUTING_TO relationship: (Project)<-[:CONTRIBUTING_TO]-(User)
     */
    public void addProjectContributor(String projectId, String userId) {
        neo4jClient.query(
            "MATCH (p:Project {id: $projectId}) " +
            "MATCH (u:User {id: $userId}) " +
            "MERGE (p)<-[:CONTRIBUTING_TO]-(u)"
        )
        .bindAll(Map.of("projectId", projectId, "userId", userId))
        .run();
    }
    
    /**
     * Removes CONTRIBUTING_TO relationship
     */
    public void removeProjectContributor(String projectId, String userId) {
        neo4jClient.query(
            "MATCH (p:Project {id: $projectId})<-[r:CONTRIBUTING_TO]-(u:User {id: $userId}) " +
            "DELETE r"
        )
        .bindAll(Map.of("projectId", projectId, "userId", userId))
        .run();
    }

    /**
     * Creates comment relationships: User -[:COMMENTED]-> Comment -[:ON]-> Post
     * This creates a Comment node in Neo4j and establishes relationships
     */
    public void createCommentRelationship(String userId, String commentId, String postId) {
        neo4jClient.query(
            "MATCH (u:User {id: $userId}) " +
            "MATCH (p:Post {id: $postId}) " +
            "CREATE (c:Comment {id: $commentId}) " +
            "CREATE (u)-[:COMMENTED]->(c) " +
            "CREATE (c)-[:ON]->(p)"
        )
        .bindAll(Map.of("userId", userId, "commentId", commentId, "postId", postId))
        .run();
    }

    /**
     * Deletes a comment and its relationships from Neo4j
     */
    public void deleteCommentRelationship(String commentId) {
        neo4jClient.query(
            "MATCH (c:Comment {id: $commentId}) " +
            "DETACH DELETE c"
        )
        .bindAll(Map.of("commentId", commentId))
        .run();
    }

}
