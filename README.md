# 🚀 DevConnect

**DevConnect** is a social collaboration platform for developers where they can connect, share posts, build projects, and find collaborators easily.

---

## ✅ Features

- ✅ User registration & profile management  
- ✅ Create, edit, and delete **Posts** and **Projects**  
- ✅ Like, share, and comment on posts  
- ✅ Tag-based project & post discovery  
- ✅ Follow other developers  
- ✅ Intelligent recommendations:
    - Suggested projects  
    - Suggested people to follow  
    - Suggested posts based on tags & interactions  
- ✅ Upload and store profile pictures and media files  
- ✅ Hybrid database system:
    - ✅ MongoDB for storing posts, users, projects, comments  
    - ✅ Neo4j for social graph relationships and recommendations   
    - ✅ Azure Blob Storage for storing media (profile pictures, project assets)

---

## 🎯 Technology Stack

| Layer | Technology |
|-------|------------|
| Frontend | React |
| Backend | Spring Boot (Java) |
| Database | MongoDB (Primary data storage) |
| Graph Database | Neo4j (Social graph & recommendations) |
| Object Storage | Azure Blob Storage (Profile pictures, media files) |
| API Design | RESTful endpoints |

---

## ✅ Architecture Overview

1. **MongoDB**  
   - Stores persistent data:  
     - `User`, `Post`, `Project`, `Comment` documents  
     - Likes, Shares stored in post document fields

2. **Neo4j**  
   - Models relationships for recommendations:
     - Users ↔ Users (Follow)
     - Users ↔ Tags (Interests)
     - Posts ↔ Tags (Discovery)
     - Projects ↔ Tags (Discovery)

3. **Azure Blob Storage**  
   - Stores user profile pictures and project assets securely in cloud storage  
   - Efficient serving of media files

4. **ElasticSearch (Future)**  
   - Full-text search of projects, posts, and users.

---

## ✅ Example Use Case Flow

### ✅ User Creates a Post
1. Post is saved in MongoDB.
2. Post tags are synced to Neo4j:  
   `PostNode` → `TAGGED_WITH` → `TagNode`

---

### ✅ User Uploads Profile Picture
1. Profile picture is uploaded from frontend.  
2. Backend uploads file to **Azure Blob Storage**.  
3. URL to the blob is saved in MongoDB under user document.

---

### ✅ User Likes a Post
1. Like count + userId updated in MongoDB Post document.
2. `UserNode -[:LIKED]-> PostNode` relationship created in Neo4j.

---

## ✅ Setup Instructions

1. Configure `application.properties`

```properties
# Server port
server.port=8000

# JWT Secret (env var fallback or hardcoded)
JWT_SECRET_BASE64_KEY=

# Azure Storage connection string
azure.storage.connection-string=YOUR_ACTUAL_CONNECTION_STRING

# Application name
spring.application.name=jujutsu

# OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=48755-dmheoup4s98e.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=GOCDDPdnk
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8080/login/oauth2/code/google

spring.security.oauth2.client.registration.github.client-id=O84Hq
spring.security.oauth2.client.registration.github.client-secret=18da367461c4
spring.security.oauth2.client.registration.github.scope=read:user,user:email
spring.security.oauth2.client.registration.github.redirect-uri=http://localhost:8080/login/oauth2/code/github

spring.security.oauth2.client.provider.github.authorization-uri=https://github.com/login/oauth/authorize
spring.security.oauth2.client.provider.github.token-uri=https://github.com/login/oauth/access_token
spring.security.oauth2.client.provider.github.user-info-uri=https://api.github.com/user

# MongoDB Configuration
spring.data.mongodb.uri=YOUR_ACTUAL_MONGO_URI
spring.data.mongodb.auto-index-creation=true
spring.data.mongodb.database=jujutsudb

```


## ✅ Setup Instructions

2. Install dependencies:

    ```bash
    mvn clean install
    ```

3. Run backend:

    ```bash
    mvn spring-boot:run
    ```

4. Connect React frontend to the backend API.
