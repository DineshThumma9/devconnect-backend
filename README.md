# 🚀 DevConnect

Real-time social collaboration platform for developers. Connect, share posts, build projects, discover collaborators, and get intelligent recommendations.

## ✅ Features

- 🔐 **Auth**: Email + JWT, OAuth (Google/GitHub)
- 📱 **Social**: Posts, projects, likes, comments, follow, tags, notifications
- 🤖 **Recommendations**: Neo4j-powered social graph, personalized post/project/collaborator suggestions
- 📡 **Real-Time**: WebSocket for live feed, chat, notifications
- 🗂️ **Data**: MongoDB (primary), Redis (caching), Neo4j (graph), Supabase (media)
- 📤 **Media**: Upload & store files via Supabase

---

## 🎯 Technology Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React |
| **Backend** | Spring Boot (Java) |
| **Primary Database** | MongoDB (Posts, Users, Projects, Comments) |
| **Cache Layer** | Redis (Home feed, frequently accessed data) |
| **Graph Database** | Neo4j (Social graph & recommendation engine) |
| **Object Storage** | Supabase Storage (Media files & assets) |
| **Real-Time Communication** | WebSocket |
| **Authentication** | JWT + OAuth 2.0 (Google, GitHub) |
| **API Design** | RESTful + WebSocket endpoints |
| **Notifications** | Event-driven notification service |

---

## 🏗️ Architecture

**Authentication**: JWT tokens + OAuth 2.0 (Google, GitHub), email login

**Dual API Layer**: 
- HTTP/REST for CRUD operations
- WebSocket for real-time updates, chat, notifications

**Multi-Database**:
- **MongoDB**: User profiles, posts, projects, comments, notifications, chat history
- **Redis**: Home feed cache (5-10 min), user profiles, recommendations cache (30 min)
- **Neo4j**: Social graph (FOLLOWS, LIKED, COMMENTED_ON, INTERESTED_IN, TAGGED_WITH relationships); powers post/collaborator recommendations
- **Supabase**: Media storage (profile pics, post images, project assets)

**Real-Time Events**: New posts, likes, comments, follows, messages broadcast via WebSocket

## 📊 Key Flows

| Flow | Details |
|------|---------|
| **Create Post** | HTTP POST → MongoDB → Neo4j sync → Redis invalidate → WebSocket broadcast → Notifications |
| **View Home Feed** | Redis cache hit/miss → MongoDB + Neo4j query → Cache results → Live WebSocket updates |
| **Recommendations** | Neo4j traversal (similar users, liked posts, interests) → Ranked results |
| **Upload Media** | Supabase Storage → URL saved to MongoDB → Redis cache invalidate |
| **Real-Time Chat** | WebSocket → MongoDB save → Broadcast to recipient → Notification |

## ✅ Setup

**Prerequisites**: Java 11+, MongoDB, Redis, Neo4j, Supabase, OAuth credentials (Google/GitHub)

**Steps**:
1. Copy config: `cp src/main/resources/application.yml.example src/main/resources/application.yml`
2. Add credentials to `application.yml` (MongoDB, Redis, Neo4j, Supabase, OAuth)
3. Install: `mvn clean install`
4. Run: `mvn spring-boot:run`
5. Access: `http://localhost:8080` | WebSocket: `ws://localhost:8080/ws` | Docs: `http://localhost:8080/swagger-ui.html`

---

## 📚 API Endpoints

### **Authentication**
- `POST /api/auth/register` - Register with email
- `POST /api/auth/login` - Login with email/password
- `POST /api/auth/oauth/google` - OAuth login (Google)
- `POST /api/auth/oauth/github` - OAuth login (GitHub)

### **Posts**
- `GET /api/posts` - Get all posts
- `GET /api/feed` - Get personalized feed (with cache)
- `POST /api/posts` - Create post
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post
- `POST /api/posts/{id}/like` - Like a post
- `POST /api/posts/{id}/comment` - Comment on post

### **Users**
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile
- `POST /api/users/{id}/follow` - Follow user
- `GET /api/users/recommendations` - Get suggested users

### **Projects**
- `GET /api/projects` - Get all projects
- `POST /api/projects` - Create project
- `PUT /api/projects/{id}` - Update project
- `DELETE /api/projects/{id}` - Delete project

### **Notifications**
- `GET /api/notifications` - Get user notifications
- `PUT /api/notifications/{id}/read` - Mark as read
- `DELETE /api/notifications/{id}` - Delete notification

### **Chat (WebSocket)**
- `WS /ws` - Connect to WebSocket
- Events: `message`, `notification`, `post_update`, `user_update`


