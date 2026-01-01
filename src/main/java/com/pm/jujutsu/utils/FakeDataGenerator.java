package com.pm.jujutsu.utils;

import com.github.javafaker.Faker;
import com.pm.jujutsu.dtos.*;
import com.pm.jujutsu.model.*;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class FakeDataGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();
    
    // Realistic tech interests/skills
    private static final List<String> TECH_INTERESTS = Arrays.asList(
        "Java", "Python", "JavaScript", "TypeScript", "React", "Angular", "Vue",
        "Node.js", "Spring Boot", "Django", "Flask", "Express.js",
        "MongoDB", "PostgreSQL", "MySQL", "Redis", "Docker", "Kubernetes",
        "AWS", "Azure", "GCP", "Git", "CI/CD", "DevOps",
        "Machine Learning", "Data Science", "AI", "Deep Learning",
        "Mobile Development", "Android", "iOS", "Flutter", "React Native",
        "Game Development", "Unity", "Unreal Engine",
        "Cybersecurity", "Blockchain", "Web3", "Microservices",
        "GraphQL", "REST API", "gRPC", "WebSockets",
        "HTML", "CSS", "Sass", "Tailwind CSS", "Bootstrap",
        "C++", "C#", "Go", "Rust", "Ruby", "PHP", "Swift", "Kotlin"
    );
    
    // Helper method to get random tech interests
    private Set<String> getRandomTechInterests(int count) {
        Set<String> interests = new HashSet<>();
        List<String> shuffled = new ArrayList<>(TECH_INTERESTS);
        Collections.shuffle(shuffled);
        
        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            interests.add(shuffled.get(i));
        }
        
        return interests;
    }

    // Generate fake User
    public User generateFakeUser() {
        User user = new User();
        user.setId(new ObjectId());
        user.setName(faker.name().fullName());
        user.setEmail(faker.internet().emailAddress());
        user.setUsername(faker.name().username());
        user.setHashedPassword(faker.internet().password());
        user.setProfilePicUrl(faker.internet().avatar());
        
        // Generate random interests (3-7 tech interests)
        user.setInterests(getRandomTechInterests(random.nextInt(5) + 3));
        
        return user;
    }

    // Generate fake UserRequestDTO
    public UserRequestDTO generateFakeUserRequestDTO() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName(faker.name().fullName());
        dto.setEmail(faker.internet().emailAddress());
        dto.setUsername(faker.name().username());
        dto.setPassword(faker.internet().password(8, 16, true, true, true));
        dto.setProfilePicUrl(faker.internet().avatar());
        
        dto.setInterests(getRandomTechInterests(random.nextInt(5) + 3));
        
        return dto;
    }

    // Generate fake Post
    public Post generateFakePost() {
        Post post = new Post();
        post.setId(new ObjectId());
        post.setOwnerId(new ObjectId());
        post.setTitle(faker.lorem().sentence(5));
        post.setContent(faker.lorem().paragraph(3));
        post.setCreatedAt(faker.date().past(30, java.util.concurrent.TimeUnit.DAYS));
        post.setUpdatedAt(new Date());
        
        // Generate random tags (2-5 tech tags)
        post.setTags(getRandomTechInterests(random.nextInt(4) + 2));
        
        // Generate random media URLs
        String[] media = IntStream.range(0, random.nextInt(3))
                .mapToObj(i -> faker.internet().image())
                .toArray(String[]::new);
        post.setMedia(media);
        
        post.setLikes(random.nextInt(100));
        post.setCommentsCount(random.nextInt(50));
        post.setShares(random.nextInt(20));
        
        return post;
    }

    // Generate fake PostRequestDTO
    public PostRequestDTO generateFakePostRequestDTO() {
        PostRequestDTO dto = new PostRequestDTO();
        dto.setTitle(faker.lorem().sentence(5));
        dto.setContent(faker.lorem().paragraph(3));
        
        dto.setTags(getRandomTechInterests(random.nextInt(4) + 2));
        
        String[] media = IntStream.range(0, random.nextInt(3))
                .mapToObj(i -> faker.internet().image())
                .toArray(String[]::new);
        dto.setMedia(media);
        
        return dto;
    }

    // Generate fake Project
    public Project generateFakeProject() {
        Project project = new Project();
        project.setId(new ObjectId());
        project.setOwnerId(new ObjectId());
        project.setTitle(faker.app().name());
        project.setDescription(faker.lorem().paragraph(2));
        project.setPrivate(random.nextBoolean());
        project.setStatus(random.nextBoolean() ? "active" : "completed");
        
        // Generate tech requirements (3-8 technologies)
        project.setTechRequirements(getRandomTechInterests(random.nextInt(6) + 3));
        
        // Generate random contributor IDs
        Set<ObjectId> contributors = IntStream.range(0, random.nextInt(5))
                .mapToObj(i -> new ObjectId())
                .collect(Collectors.toSet());
        project.setCurrentContributorIds(contributors);
        
        return project;
    }

    // Generate fake ProjectRequestDTO
    public ProjectRequestDTO generateFakeProjectRequestDTO() {
        ProjectRequestDTO dto = new ProjectRequestDTO();
        dto.setTitle(faker.app().name());
        dto.setDescription(faker.lorem().paragraph(2));
        dto.setPrivate(random.nextBoolean());

        
        dto.setTechRequirements(getRandomTechInterests(random.nextInt(6) + 3));
        
        return dto;
    }

    // Generate fake Comment
    public Comment generateFakeComment() {
        Comment comment = new Comment();
        comment.setId(new ObjectId());
        comment.setUserId(new ObjectId());
        comment.setCreatedAt(faker.date().past(10, java.util.concurrent.TimeUnit.DAYS));
        return comment;
    }

    // Generate multiple fake users
    public List<User> generateFakeUsers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> generateFakeUser())
                .collect(Collectors.toList());
    }

    // Generate multiple fake posts
    public List<Post> generateFakePosts(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> generateFakePost())
                .collect(Collectors.toList());
    }

    // Generate multiple fake projects
    public List<Project> generateFakeProjects(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> generateFakeProject())
                .collect(Collectors.toList());
    }
}
