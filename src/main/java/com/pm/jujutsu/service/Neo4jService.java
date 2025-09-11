package com.pm.jujutsu.service;

import com.pm.jujutsu.repository.PostNodeRespository;
import com.pm.jujutsu.repository.ProjectNodeRepository;
import com.pm.jujutsu.repository.UserNodeRepository;
import com.pm.jujutsu.utils.JwtUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private JwtUtil jwtUtil;


    // ---------------- User Tag Sync ----------------
    public void syncUserTags(ObjectId userId, Set<String> tags) {
        userNodeRepository.syncUserTags(userId, tags);
    }


    public void syncPostTags(ObjectId postId, Set<String> tags) {
        postNodeRespository.syncPostTags(postId, tags);
    }

    public void syncProjectTags(ObjectId projectId, Set<String> tags) {
        projectNodeRepository.syncProjectTags(projectId, tags);
    }


    public List<String> recommendPostBasedOnTags(ObjectId userId, Set<String> tags) {
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
}
