# Test Data Seeding Guide

This guide explains how to populate your DevConnect application with fake test data for testing recommendations and other features.

## 🚀 Quick Start

To seed everything at once:

```bash
POST http://localhost:8080/dev/seed/all
```

This will create:
- 10 users with interests
- 20 posts with tags
- 10 projects with tech requirements
- 15 follow relationships

You can customize the counts:
```bash
POST http://localhost:8080/dev/seed/all?userCount=20&postCount=50&projectCount=15&followCount=30
```

## 📋 Individual Seeding Endpoints

### 1. Seed Users
Creates users and syncs them to Neo4j with their interests.

```bash
POST http://localhost:8080/dev/seed/users/10
```

**What it does:**
- Creates 10 fake users in MongoDB
- Creates corresponding User nodes in Neo4j
- Creates `INTERESTED_IN` relationships to Tag nodes
- All users have password: `password123`

**Response:**
```json
{
  "success": true,
  "count": 10,
  "users": [
    {
      "id": "507f1f77bcf86cd799439011",
      "username": "john_doe_1234567890_0",
      "email": "john_doe_1234567890_0@example.com",
      "interests": ["Java", "Python", "React"]
    }
  ],
  "note": "All users created with password: password123"
}
```

### 2. Seed Posts
Creates posts with tags and syncs them to Neo4j.

```bash
POST http://localhost:8080/dev/seed/posts/20
```

Optional: Specify a user ID to create posts for a specific user:
```bash
POST http://localhost:8080/dev/seed/posts/20?userId=507f1f77bcf86cd799439011
```

**What it does:**
- Creates 20 fake posts in MongoDB
- Assigns random existing users as owners
- Creates Post nodes in Neo4j
- Creates `TAGGED_WITH` relationships to Tag nodes

### 3. Seed Projects
Creates projects with tech requirements and syncs them to Neo4j.

```bash
POST http://localhost:8080/dev/seed/projects/10
```

**What it does:**
- Creates 10 fake projects in MongoDB
- Assigns random existing users as owners
- Creates Project nodes in Neo4j
- Creates `WORK_WITH` relationships to Tag nodes
- Creates `OWNED_BY` relationships to User nodes

### 4. Seed Follow Relationships
Creates random follow relationships between existing users.

```bash
POST http://localhost:8080/dev/seed/follows/15
```

**What it does:**
- Creates 15 random follow relationships
- Updates MongoDB user documents (followingIds, followerIds)
- Creates `FOLLOWS` relationships in Neo4j

**Note:** Requires at least 2 users to exist first.

## 🗑️ Clear All Data

**⚠️ USE WITH CAUTION - This deletes all data from MongoDB**

```bash
DELETE http://localhost:8080/dev/seed/clear
```

**Important:** This only clears MongoDB. To clear Neo4j data, use Neo4j Browser:
```cypher
MATCH (n) DETACH DELETE n
```

## 📊 Viewing Generated Data (Read-Only Endpoints)

These endpoints just generate data without saving it (useful for testing the generator):

```bash
# Generate single fake objects
GET http://localhost:8080/dev/fake-user
GET http://localhost:8080/dev/fake-post
GET http://localhost:8080/dev/fake-project

# Generate multiple fake objects
GET http://localhost:8080/dev/fake-users/5
GET http://localhost:8080/dev/fake-posts/10
GET http://localhost:8080/dev/fake-projects/3

# Generate all types
GET http://localhost:8080/dev/fake-all
```

## 🎯 Recommended Testing Workflow

1. **Start fresh (optional):**
   ```bash
   DELETE http://localhost:8080/dev/seed/clear
   ```

2. **Seed all data:**
   ```bash
   POST http://localhost:8080/dev/seed/all?userCount=15&postCount=30&projectCount=15&followCount=25
   ```

3. **Test recommendations:**
   ```bash
   # Get a user from the seeded data
   GET http://localhost:8080/feed/suggestions/{username}
   
   # Test project recommendations (requires authentication)
   GET http://localhost:8080/feed/recommendations
   
   # Test user recommendations
   GET http://localhost:8080/feed/suggested-connections/{username}
   ```

4. **Login with a test user:**
   ```bash
   POST http://localhost:8080/auth/login
   {
     "username": "john_doe_1234567890_0",
     "password": "password123"
   }
   ```

## 📝 Understanding the Data Structure

### Users
- **MongoDB:** User documents with interests
- **Neo4j:** User nodes with `INTERESTED_IN` relationships to Tag nodes

### Posts
- **MongoDB:** Post documents with tags
- **Neo4j:** Post nodes with `TAGGED_WITH` relationships to Tag nodes

### Projects
- **MongoDB:** Project documents with tech requirements
- **Neo4j:** Project nodes with:
  - `WORK_WITH` relationships to Tag nodes
  - `OWNED_BY` relationships to User nodes

### Relationships
- **FOLLOWS:** User → User (who follows whom)
- **SUBSCRIBE:** User → Project (user subscribed to project)
- **INTERESTED_IN:** User → Tag (user interests)
- **WORK_WITH:** Project → Tag (project tech requirements)
- **TAGGED_WITH:** Post → Tag (post tags)

## 🔍 Debugging Tips

All seeding endpoints print console logs:
```
✅ Created user: john_doe with interests: [Java, React, Node.js]
✅ Created post: My First Post with tags: [Python, Django]
✅ Created project: Awesome App with tech: [React, TypeScript, Node.js]
✅ john_doe now follows jane_smith
```

Check your application logs to see what's being created.

## 🐛 Troubleshooting

### "No users found" error when seeding posts/projects
**Solution:** Create users first:
```bash
POST http://localhost:8080/dev/seed/users/10
```

### "Need at least 2 users" error when seeding follows
**Solution:** Create more users first:
```bash
POST http://localhost:8080/dev/seed/users/5
```

### Recommendations not working
1. Verify Neo4j is running
2. Check that relationships are created in Neo4j Browser
3. Look at the debug logs added to ProjectService and UserService
4. Ensure users have interests and projects have tech requirements

## 🔧 Configuration

The fake data is generated using the JavaFaker library. The generator creates:
- Realistic names, emails, usernames
- Random programming languages as interests/tags
- Lorem ipsum content for posts/projects
- Random avatars and images

All users are created with password: `password123`

## 📚 Related Files

- **Controller:** `src/main/java/com/pm/jujutsu/controller/DevDataController.java`
- **Generator:** `src/main/java/com/pm/jujutsu/utils/FakeDataGenerator.java`
- **Services:** 
  - `src/main/java/com/pm/jujutsu/service/Neo4jService.java`
  - `src/main/java/com/pm/jujutsu/service/UserService.java`
  - `src/main/java/com/pm/jujutsu/service/PostService.java`
  - `src/main/java/com/pm/jujutsu/service/ProjectService.java`
