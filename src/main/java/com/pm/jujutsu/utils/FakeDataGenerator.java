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
    
    // Realistic post titles and content templates
    private static final List<String> POST_TITLES = Arrays.asList(
        "Just deployed my first microservices architecture!",
        "Excited to share my new open source contribution",
        "Looking for feedback on my React component library",
        "Just solved a tricky algorithm problem",
        "Sharing my experience migrating from monolith to microservices",
        "Built a real-time chat app with WebSockets",
        "My journey learning machine learning this year",
        "Tips for optimizing database queries",
        "How I improved API response time by 70%",
        "Deployed my app to production for the first time!",
        "Sharing my code review best practices",
        "Just got my AWS certification!",
        "Building a REST API with Spring Boot",
        "My experience with Test-Driven Development",
        "Containerizing applications with Docker",
        "Learning TypeScript - some thoughts",
        "Implementing OAuth2 authentication",
        "My favorite VS Code extensions",
        "Debugging production issues - lessons learned",
        "Setting up a CI/CD pipeline from scratch"
    );
    
    private static final List<String> POST_CONTENTS = Arrays.asList(
        "After weeks of hard work, I finally got this working! The key was understanding how to properly handle asynchronous operations. Would love to hear your thoughts and feedback.",
        "I've been working on this project for a few months now and learned so much. Here are some key takeaways I wanted to share with the community.",
        "This was such a challenging problem to solve! Had to dig deep into the documentation and experiment with different approaches. Finally found a solution that works great.",
        "Really excited about this new technology stack. The developer experience is amazing and the performance improvements are significant. Highly recommend checking it out!",
        "Spent the whole weekend refactoring this code. It's much cleaner now and way easier to maintain. Sometimes taking a step back to improve code quality is worth it.",
        "Just wrapped up this feature and wanted to document my approach. Hope this helps others facing similar challenges. Feel free to ask questions!",
        "Had an interesting debugging session today. The issue was subtle but taught me a lot about how this system works under the hood. Sharing what I learned.",
        "After trying several different approaches, this solution worked best for my use case. Here's why I chose this particular tech stack and architecture.",
        "Making progress on my learning journey! These resources were incredibly helpful. Dropping some links for anyone interested in learning more.",
        "This integration was trickier than expected but totally worth it. The end result is a much better user experience. Here's how I did it."
    );
    
    // Realistic project titles and descriptions
    private static final List<ProjectData> PROJECT_DATA = Arrays.asList(
        new ProjectData(
            "Task Management Platform",
            "Building a full-stack task management app with real-time collaboration features. Looking for contributors interested in React, Node.js, and WebSocket implementation.",
            Arrays.asList("React", "Node.js", "MongoDB", "WebSockets", "TypeScript")
        ),
        new ProjectData(
            "AI Code Review Assistant",
            "Developing an intelligent code review tool that uses machine learning to detect bugs and suggest improvements. Need help with ML models and VS Code extension development.",
            Arrays.asList("Python", "Machine Learning", "TypeScript", "Node.js")
        ),
        new ProjectData(
            "Social Media Analytics Dashboard",
            "Creating a dashboard to visualize social media metrics and trends. Looking for frontend developers and data visualization experts.",
            Arrays.asList("React", "D3.js", "Python", "PostgreSQL", "REST API")
        ),
        new ProjectData(
            "E-Commerce Mobile App",
            "Building a cross-platform mobile shopping app with payment integration. Need mobile developers and backend engineers.",
            Arrays.asList("React Native", "Node.js", "MongoDB", "Stripe API", "Redux")
        ),
        new ProjectData(
            "Real-Time Chat Application",
            "Developing a secure messaging app with end-to-end encryption. Looking for contributors with security and real-time system experience.",
            Arrays.asList("WebSockets", "Node.js", "React", "Encryption", "Redis")
        ),
        new ProjectData(
            "Fitness Tracking API",
            "Creating a comprehensive REST API for fitness and health data. Need backend developers familiar with Spring Boot and database optimization.",
            Arrays.asList("Java", "Spring Boot", "PostgreSQL", "REST API", "Docker")
        ),
        new ProjectData(
            "Open Source Blog Platform",
            "Building a modern, lightweight blogging platform with Markdown support. Looking for full-stack developers and UI/UX contributors.",
            Arrays.asList("Vue", "Express.js", "MongoDB", "Markdown", "Tailwind CSS")
        ),
        new ProjectData(
            "Automated Testing Framework",
            "Developing a framework for automated browser testing. Need contributors with testing experience and browser automation knowledge.",
            Arrays.asList("Python", "Selenium", "Docker", "CI/CD", "pytest")
        ),
        new ProjectData(
            "Weather Forecast App",
            "Creating a beautiful weather app with location-based forecasts. Looking for mobile developers and API integration experts.",
            Arrays.asList("Flutter", "REST API", "Firebase", "Google Maps API")
        ),
        new ProjectData(
            "Developer Portfolio Generator",
            "Building a tool to automatically generate portfolio websites from GitHub data. Need frontend and GitHub API experts.",
            Arrays.asList("React", "Node.js", "GitHub API", "Next.js", "GraphQL")
        ),
        new ProjectData(
            "Cloud Infrastructure Monitor",
            "Developing a monitoring solution for cloud resources. Looking for DevOps engineers and cloud platform specialists.",
            Arrays.asList("Python", "AWS", "Docker", "Kubernetes", "Prometheus")
        ),
        new ProjectData(
            "Recipe Sharing Platform",
            "Creating a social platform for sharing and discovering recipes. Need full-stack developers and database designers.",
            Arrays.asList("React", "Spring Boot", "PostgreSQL", "AWS", "ElasticSearch")
        ),
        new ProjectData(
            "Game Development Engine",
            "Building a 2D game engine for indie developers. Looking for contributors with game dev and graphics programming experience.",
            Arrays.asList("C++", "OpenGL", "Game Development", "Python")
        ),
        new ProjectData(
            "Expense Tracker App",
            "Developing a personal finance management app with budgeting features. Need mobile and backend developers.",
            Arrays.asList("React Native", "Node.js", "MongoDB", "Chart.js", "JWT")
        ),
        new ProjectData(
            "Video Streaming Platform",
            "Creating a video hosting and streaming service. Looking for backend engineers and video processing experts.",
            Arrays.asList("Node.js", "FFmpeg", "AWS", "React", "WebRTC")
        ),
        new ProjectData(
            "Smart Home Controller",
            "Building an IoT platform for controlling smart home devices. Need embedded systems and mobile app developers.",
            Arrays.asList("Python", "React Native", "MQTT", "Raspberry Pi", "WebSockets")
        ),
        new ProjectData(
            "Language Learning App",
            "Developing an interactive language learning platform with gamification. Looking for mobile developers and UI/UX designers.",
            Arrays.asList("Flutter", "Firebase", "REST API", "Game Development")
        ),
        new ProjectData(
            "Code Snippet Manager",
            "Creating a tool for organizing and searching code snippets with AI-powered tagging. Need full-stack and ML developers.",
            Arrays.asList("React", "Python", "ElasticSearch", "Machine Learning", "MongoDB")
        ),
        new ProjectData(
            "Music Streaming Service",
            "Building a music streaming platform with playlist recommendations. Looking for backend engineers and ML specialists.",
            Arrays.asList("React", "Node.js", "Machine Learning", "PostgreSQL", "Redis")
        ),
        new ProjectData(
            "Job Board Platform",
            "Developing a job posting and application platform for tech roles. Need full-stack developers and search optimization experts.",
            Arrays.asList("Angular", "Spring Boot", "ElasticSearch", "PostgreSQL", "AWS")
        )
    );
    
    // Helper class for project data
    private static class ProjectData {
        String title;
        String description;
        List<String> techStack;
        
        ProjectData(String title, String description, List<String> techStack) {
            this.title = title;
            this.description = description;
            this.techStack = techStack;
        }
    }
    
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
        
        // Use realistic title instead of lorem ipsum
        post.setTitle(POST_TITLES.get(random.nextInt(POST_TITLES.size())));
        
        // Use realistic content instead of lorem ipsum
        post.setContent(POST_CONTENTS.get(random.nextInt(POST_CONTENTS.size())));
        
        post.setCreatedAt(faker.date().past(30, java.util.concurrent.TimeUnit.DAYS));
        post.setUpdatedAt(new Date());
        
        // Generate random tags (2-5 tech tags)
        post.setTags(getRandomTechInterests(random.nextInt(4) + 2));
        
        // Generate random media URLs (some posts might not have media)
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
        
        // Use realistic title instead of lorem ipsum
        dto.setTitle(POST_TITLES.get(random.nextInt(POST_TITLES.size())));
        
        // Use realistic content instead of lorem ipsum
        dto.setContent(POST_CONTENTS.get(random.nextInt(POST_CONTENTS.size())));
        
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
        
        // Use realistic project data
        ProjectData projectData = PROJECT_DATA.get(random.nextInt(PROJECT_DATA.size()));
        project.setTitle(projectData.title);
        project.setDescription(projectData.description);
        
        project.setPrivate(random.nextBoolean());
        project.setStatus(random.nextBoolean() ? "active" : "completed");
        
        // Use tech stack from project data, or generate random if preferred
        Set<String> techStack = new HashSet<>(projectData.techStack);
        // Optionally add a few more random technologies
        if (random.nextBoolean()) {
            techStack.addAll(getRandomTechInterests(random.nextInt(2) + 1));
        }
        project.setTechRequirements(techStack);
        
        // Generate random contributor IDs
        Set<ObjectId> contributors = IntStream.range(0, random.nextInt(5))
                .mapToObj(i -> new ObjectId())
                .collect(Collectors.toSet());
        project.setCurrentContributorIds(contributors);
        
        // Add realistic GitHub link sometimes
        if (random.nextDouble() > 0.3) {
            String repoName = projectData.title.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-");
            project.setGithubLink("https://github.com/" + faker.name().username() + "/" + repoName);
        }
        
        return project;
    }

    // Generate fake ProjectRequestDTO
    public ProjectRequestDTO generateFakeProjectRequestDTO() {
        ProjectRequestDTO dto = new ProjectRequestDTO();
        
        // Use realistic project data
        ProjectData projectData = PROJECT_DATA.get(random.nextInt(PROJECT_DATA.size()));
        dto.setTitle(projectData.title);
        dto.setDescription(projectData.description);
        
        dto.setPrivate(random.nextBoolean());
        
        // Use tech stack from project data
        Set<String> techStack = new HashSet<>(projectData.techStack);
        // Optionally add a few more random technologies
        if (random.nextBoolean()) {
            techStack.addAll(getRandomTechInterests(random.nextInt(2) + 1));
        }
        dto.setTechRequirements(techStack);
        
        // Add realistic GitHub link sometimes
        if (random.nextDouble() > 0.3) {
            String repoName = projectData.title.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-");
            dto.setGithubLink("https://github.com/" + faker.name().username() + "/" + repoName);
        }
        
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
