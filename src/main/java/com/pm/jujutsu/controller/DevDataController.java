package com.pm.jujutsu.controller;

import com.pm.jujutsu.dtos.*;
import com.pm.jujutsu.model.*;
import com.pm.jujutsu.service.UserService;
import com.pm.jujutsu.service.PostService;
import com.pm.jujutsu.service.ProjectService;
import com.pm.jujutsu.service.Neo4jService;
import com.pm.jujutsu.repository.UserRepository;
import com.pm.jujutsu.repository.PostRepository;
import com.pm.jujutsu.repository.ProjectRepository;
import com.pm.jujutsu.utils.Encoder;
import com.pm.jujutsu.utils.FakeDataGenerator;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dev")
public class DevDataController {

    @Autowired
    private FakeDataGenerator fakeDataGenerator;
    
    @Autowired
    private Encoder encoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private Neo4jService neo4jService;

    @GetMapping("/fake-user")
    public ResponseEntity<Map<String, Object>> generateFakeUser() {
        Map<String, Object> response = new HashMap<>();
        
        User user = fakeDataGenerator.generateFakeUser();
        UserRequestDTO userRequestDTO = fakeDataGenerator.generateFakeUserRequestDTO();
        
        response.put("userModel", user);
        response.put("userRequestDTO", userRequestDTO);
        response.put("description", "Generated fake user data - Model vs DTO");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-users/{count}")
    public ResponseEntity<Map<String, Object>> generateFakeUsers(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        
        List<User> users = fakeDataGenerator.generateFakeUsers(count);
        
        response.put("users", users);
        response.put("count", users.size());
        response.put("description", "Generated " + count + " fake users");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-post")
    public ResponseEntity<Map<String, Object>> generateFakePost() {
        Map<String, Object> response = new HashMap<>();
        
        Post post = fakeDataGenerator.generateFakePost();
        PostRequestDTO postRequestDTO = fakeDataGenerator.generateFakePostRequestDTO();
        
        response.put("postModel", post);
        response.put("postRequestDTO", postRequestDTO);
        response.put("description", "Generated fake post data - Model vs DTO");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-posts/{count}")
    public ResponseEntity<Map<String, Object>> generateFakePosts(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        
        List<Post> posts = fakeDataGenerator.generateFakePosts(count);
        
        response.put("posts", posts);
        response.put("count", posts.size());
        response.put("description", "Generated " + count + " fake posts");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-project")
    public ResponseEntity<Map<String, Object>> generateFakeProject() {
        Map<String, Object> response = new HashMap<>();
        
        Project project = fakeDataGenerator.generateFakeProject();
        ProjectRequestDTO projectRequestDTO = fakeDataGenerator.generateFakeProjectRequestDTO();
        
        response.put("projectModel", project);
        response.put("projectRequestDTO", projectRequestDTO);
        response.put("description", "Generated fake project data - Model vs DTO");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-projects/{count}")
    public ResponseEntity<Map<String, Object>> generateFakeProjects(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        
        List<Project> projects = fakeDataGenerator.generateFakeProjects(count);
        
        response.put("projects", projects);
        response.put("count", projects.size());
        response.put("description", "Generated " + count + " fake projects");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-comment")
    public ResponseEntity<Map<String, Object>> generateFakeComment() {
        Map<String, Object> response = new HashMap<>();
        
        Comment comment = fakeDataGenerator.generateFakeComment();
        
        response.put("commentModel", comment);
        response.put("description", "Generated fake comment data");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fake-all")
    public ResponseEntity<Map<String, Object>> generateAllFakeData() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("users", fakeDataGenerator.generateFakeUsers(3));
        response.put("posts", fakeDataGenerator.generateFakePosts(5));
        response.put("projects", fakeDataGenerator.generateFakeProjects(3));
        response.put("comments", List.of(
                fakeDataGenerator.generateFakeComment(),
                fakeDataGenerator.generateFakeComment(),
                fakeDataGenerator.generateFakeComment()
        ));
        
        response.put("description", "Complete set of fake data for all models");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(
            @RequestParam String password,
            @RequestParam String hash) {
        Map<String, Object> response = new HashMap<>();
        
        boolean matches = encoder.matches(password, hash);
        
        response.put("password", password);
        response.put("hash", hash);
        response.put("matches", matches);
        response.put("description", "Password verification test");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== PERSISTENCE ENDPOINTS ====================
    
    /**
     * Create and persist fake users to MongoDB and Neo4j
     * This creates users with interests and syncs them to Neo4j
     */
    @PostMapping("/seed/users/{count}")
    public ResponseEntity<Map<String, Object>> seedUsers(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        List<User> createdUsers = new ArrayList<>();
        
        try {
            for (int i = 0; i < count; i++) {
                User user = fakeDataGenerator.generateFakeUser();
                
                // Hash the password
                user.setHashedPassword(encoder.encode("password123"));
                
                // Ensure unique username and email
                user.setUsername(user.getUsername() + "_" + System.currentTimeMillis() + "_" + i);
                user.setEmail(user.getUsername() + "@example.com");
                
                // Save to MongoDB
                User savedUser = userRepository.save(user);
                createdUsers.add(savedUser);
                
                // Create Neo4j node and sync interests
                neo4jService.createUserNode(savedUser.getId().toHexString());
                if (savedUser.getInterests() != null && !savedUser.getInterests().isEmpty()) {
                    neo4jService.syncUserTags(savedUser.getId().toHexString(), savedUser.getInterests());
                }
                
                System.out.println("✅ Created user: " + savedUser.getUsername());
                System.out.println("   📧 Email: " + savedUser.getEmail());
                System.out.println("   🔑 Password: password123");
                System.out.println("   🏷️  Interests: " + savedUser.getInterests());
            }
            
            response.put("success", true);
            response.put("count", createdUsers.size());
            response.put("users", createdUsers.stream()
                    .map(u -> Map.of(
                            "id", u.getId().toHexString(),
                            "username", u.getUsername(),
                            "email", u.getEmail(),
                            "password", "password123",
                            "interests", u.getInterests() != null ? u.getInterests() : Set.of()
                    ))
                    .collect(Collectors.toList()));
            response.put("note", "All users created with password: password123");
            response.put("loginInstructions", "Use either 'username' or 'email' with password 'password123' to login");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("createdCount", createdUsers.size());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Create and persist fake posts to MongoDB and Neo4j
     * This creates posts with tags and syncs them to Neo4j
     */
    @PostMapping("/seed/posts/{count}")
    public ResponseEntity<Map<String, Object>> seedPosts(
            @PathVariable int count,
            @RequestParam(required = false) String userId) {
        Map<String, Object> response = new HashMap<>();
        List<Post> createdPosts = new ArrayList<>();
        
        try {
            // If no userId provided, get a random user or use the first one
            ObjectId ownerId;
            if (userId != null && !userId.isEmpty()) {
                ownerId = new ObjectId(userId);
            } else {
                // Get all users and pick random ones
                List<User> users = userRepository.findAll();
                if (users.isEmpty()) {
                    response.put("success", false);
                    response.put("error", "No users found. Please create users first using /dev/seed/users/{count}");
                    return ResponseEntity.badRequest().body(response);
                }
                ownerId = users.get(new Random().nextInt(users.size())).getId();
            }
            
            for (int i = 0; i < count; i++) {
                Post post = fakeDataGenerator.generateFakePost();
                
                // Set owner - either provided userId or random
                List<User> users = userRepository.findAll();
                if (!users.isEmpty()) {
                    post.setOwnerId(users.get(new Random().nextInt(users.size())).getId());
                } else {
                    post.setOwnerId(ownerId);
                }
                
                // Save to MongoDB
                Post savedPost = postRepository.save(post);
                createdPosts.add(savedPost);
                
                // Create Neo4j node and sync tags
                neo4jService.createPostNode(savedPost.getId().toHexString());
                if (savedPost.getTags() != null && !savedPost.getTags().isEmpty()) {
                    neo4jService.syncPostTags(savedPost.getId().toHexString(), savedPost.getTags());
                }
                
                System.out.println("✅ Created post: " + savedPost.getTitle() + " with tags: " + savedPost.getTags());
            }
            
            response.put("success", true);
            response.put("count", createdPosts.size());
            response.put("posts", createdPosts.stream()
                    .map(p -> Map.of(
                            "id", p.getId().toHexString(),
                            "title", p.getTitle(),
                            "ownerId", p.getOwnerId().toHexString(),
                            "tags", p.getTags() != null ? p.getTags() : Set.of(),
                            "likes", p.getLikes()
                    ))
                    .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("createdCount", createdPosts.size());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Create and persist fake projects to MongoDB and Neo4j
     * This creates projects with tech requirements and syncs them to Neo4j
     */
    @PostMapping("/seed/projects/{count}")
    public ResponseEntity<Map<String, Object>> seedProjects(
            @PathVariable int count,
            @RequestParam(required = false) String userId) {
        Map<String, Object> response = new HashMap<>();
        List<Project> createdProjects = new ArrayList<>();
        
        try {
            for (int i = 0; i < count; i++) {
                Project project = fakeDataGenerator.generateFakeProject();
                
                // Set owner - get from existing users
                List<User> users = userRepository.findAll();
                if (users.isEmpty()) {
                    response.put("success", false);
                    response.put("error", "No users found. Please create users first using /dev/seed/users/{count}");
                    return ResponseEntity.badRequest().body(response);
                }
                
                User owner = users.get(new Random().nextInt(users.size()));
                project.setOwnerId(owner.getId());
                
                // Save to MongoDB
                Project savedProject = projectRepository.save(project);
                createdProjects.add(savedProject);
                
                // Create Neo4j node and relationships
                neo4jService.createProjectNode(
                        savedProject.getId().toHexString(),
                        savedProject.getTitle(),
                        savedProject.getDescription()
                );
                
                neo4jService.createProjectOwnerRelationship(
                        savedProject.getId().toHexString(),
                        savedProject.getOwnerId().toHexString()
                );
                
                if (savedProject.getTechRequirements() != null && !savedProject.getTechRequirements().isEmpty()) {
                    neo4jService.syncProjectTags(savedProject.getId().toHexString(), savedProject.getTechRequirements());
                }
                
                System.out.println("✅ Created project: " + savedProject.getTitle() + " with tech: " + savedProject.getTechRequirements());
            }
            
            response.put("success", true);
            response.put("count", createdProjects.size());
            response.put("projects", createdProjects.stream()
                    .map(p -> Map.of(
                            "id", p.getId().toHexString(),
                            "title", p.getTitle(),
                            "ownerId", p.getOwnerId().toHexString(),
                            "techRequirements", p.getTechRequirements() != null ? p.getTechRequirements() : Set.of(),
                            "status", p.getStatus() != null ? p.getStatus() : "active"
                    ))
                    .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("createdCount", createdProjects.size());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Create random follow relationships between users
     */
    @PostMapping("/seed/follows/{count}")
    public ResponseEntity<Map<String, Object>> seedFollows(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> relationships = new ArrayList<>();
        
        try {
            List<User> users = userRepository.findAll();
            if (users.size() < 2) {
                response.put("success", false);
                response.put("error", "Need at least 2 users to create follow relationships");
                return ResponseEntity.badRequest().body(response);
            }
            
            Random random = new Random();
            for (int i = 0; i < count; i++) {
                User follower = users.get(random.nextInt(users.size()));
                User following = users.get(random.nextInt(users.size()));
                
                // Don't follow yourself
                if (follower.getId().equals(following.getId())) {
                    continue;
                }
                
                // Add to MongoDB
                if (follower.getFollowingIds() == null) {
                    follower.setFollowingIds(new HashSet<>());
                }
                if (following.getFollowerIds() == null) {
                    following.setFollowerIds(new HashSet<>());
                }
                
                follower.getFollowingIds().add(following.getId());
                following.getFollowerIds().add(follower.getId());
                
                userRepository.save(follower);
                userRepository.save(following);
                
                // Add to Neo4j
                neo4jService.followRelationship(
                        follower.getId().toHexString(),
                        following.getId().toHexString()
                );
                
                relationships.add(Map.of(
                        "follower", follower.getUsername(),
                        "following", following.getUsername()
                ));
                
                System.out.println("✅ " + follower.getUsername() + " now follows " + following.getUsername());
            }
            
            response.put("success", true);
            response.put("count", relationships.size());
            response.put("relationships", relationships);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Create random subscribe relationships (User -> Project)
     * This is CRITICAL for project recommendations to work!
     */
    @PostMapping("/seed/subscriptions/{count}")
    public ResponseEntity<Map<String, Object>> seedSubscriptions(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> relationships = new ArrayList<>();
        
        try {
            List<User> users = userRepository.findAll();
            List<Project> projects = projectRepository.findAll();
            
            if (users.isEmpty()) {
                response.put("success", false);
                response.put("error", "No users found. Please create users first using /dev/seed/users/{count}");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (projects.isEmpty()) {
                response.put("success", false);
                response.put("error", "No projects found. Please create projects first using /dev/seed/projects/{count}");
                return ResponseEntity.badRequest().body(response);
            }
            
            Random random = new Random();
            Set<String> createdRelationships = new HashSet<>();
            
            for (int i = 0; i < count; i++) {
                User user = users.get(random.nextInt(users.size()));
                Project project = projects.get(random.nextInt(projects.size()));
                
                // Avoid duplicates
                String relationshipKey = user.getId().toHexString() + "-" + project.getId().toHexString();
                if (createdRelationships.contains(relationshipKey)) {
                    continue;
                }
                createdRelationships.add(relationshipKey);
                
                // Create SUBSCRIBE relationship in Neo4j
                neo4jService.subscribeToProject(
                        user.getId().toHexString(),
                        project.getId().toHexString()
                );
                
                relationships.add(Map.of(
                        "user", user.getUsername(),
                        "project", project.getTitle()
                ));
                
                System.out.println("✅ " + user.getUsername() + " subscribed to project: " + project.getTitle());
            }
            
            response.put("success", true);
            response.put("count", relationships.size());
            response.put("relationships", relationships);
            response.put("note", "These SUBSCRIBE relationships are critical for project recommendations!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Seed everything - users, posts, projects, and relationships
     */
    @PostMapping("/seed/all")
    public ResponseEntity<Map<String, Object>> seedAll(
            @RequestParam(defaultValue = "10") int userCount,
            @RequestParam(defaultValue = "20") int postCount,
            @RequestParam(defaultValue = "10") int projectCount,
            @RequestParam(defaultValue = "15") int followCount,
            @RequestParam(defaultValue = "20") int subscriptionCount) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Seed users
            ResponseEntity<Map<String, Object>> usersResponse = seedUsers(userCount);
            
            // Seed posts
            ResponseEntity<Map<String, Object>> postsResponse = seedPosts(postCount, null);
            
            // Seed projects
            ResponseEntity<Map<String, Object>> projectsResponse = seedProjects(projectCount, null);
            
            // Seed follows
            ResponseEntity<Map<String, Object>> followsResponse = seedFollows(followCount);
            
            // Seed subscriptions (CRITICAL for recommendations!)
            ResponseEntity<Map<String, Object>> subscriptionsResponse = seedSubscriptions(subscriptionCount);
            
            response.put("success", true);
            response.put("users", usersResponse.getBody());
            response.put("posts", postsResponse.getBody());
            response.put("projects", projectsResponse.getBody());
            response.put("follows", followsResponse.getBody());
            response.put("subscriptions", subscriptionsResponse.getBody());
            response.put("message", "Successfully seeded all test data including SUBSCRIBE relationships!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Clear all test data (USE WITH CAUTION!)
     */
    @DeleteMapping("/seed/clear")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long userCount = userRepository.count();
            long postCount = postRepository.count();
            long projectCount = projectRepository.count();
            
            userRepository.deleteAll();
            postRepository.deleteAll();
            projectRepository.deleteAll();
            
            response.put("success", true);
            response.put("deleted", Map.of(
                    "users", userCount,
                    "posts", postCount,
                    "projects", projectCount
            ));
            response.put("warning", "Neo4j data NOT automatically cleared. Use Neo4j Browser to clear if needed: MATCH (n) DETACH DELETE n");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
