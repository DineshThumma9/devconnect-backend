# Supabase Storage Quick Reference

## Configuration Properties

Add these to your `application.properties` or environment variables:

```properties
supabase.url=https://YOUR_PROJECT_ID.supabase.co
supabase.key=YOUR_ANON_KEY
supabase.storage.bucket=YOUR_BUCKET_NAME
```

## Environment Variables (Recommended for Production)

```bash
export SUPABASE_URL=https://YOUR_PROJECT_ID.supabase.co
export SUPABASE_KEY=YOUR_ANON_KEY
export SUPABASE_BUCKET=YOUR_BUCKET_NAME
```

Then update `application.properties`:
```properties
supabase.url=${SUPABASE_URL}
supabase.key=${SUPABASE_KEY}
supabase.storage.bucket=${SUPABASE_BUCKET}
```

## SupabaseStorageService Methods

```java
// Upload a file
String url = supabaseStorageService.uploadFile(multipartFile);

// Download a file
byte[] fileContent = supabaseStorageService.downloadFile("filename.jpg");

// Delete a file
supabaseStorageService.deleteFile("filename.jpg");

// List all files
List<String> files = supabaseStorageService.listFiles();

// Get public URL
String publicUrl = supabaseStorageService.getPublicUrl("filename.jpg");

// Get bucket base URL
String bucketUrl = supabaseStorageService.getBucketUrl();
```

## API Endpoints

### Upload Profile Picture
```bash
POST /users/upload-profile-picture
Content-Type: multipart/form-data
Authorization: Bearer YOUR_JWT_TOKEN

Body:
- file: [binary file data]
```

### Register with Profile Picture
```bash
POST /auth/register-with-profile-pic
Content-Type: multipart/form-data

Body:
- user: {
    "email": "user@example.com",
    "password": "password123",
    "name": "John Doe",
    "username": "johndoe"
  }
- profilePic: [binary file data] (optional)
```

## Supabase Storage Policies

### Allow Public Read
```sql
CREATE POLICY "Public Access"
ON storage.objects FOR SELECT
USING (bucket_id = 'your-bucket-name');
```

### Allow Authenticated Upload
```sql
CREATE POLICY "Authenticated Upload"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'your-bucket-name');
```

### Allow Authenticated Delete
```sql
CREATE POLICY "Authenticated Delete"
ON storage.objects FOR DELETE
USING (bucket_id = 'your-bucket-name');
```

## Common Issues & Solutions

### Issue: 401 Unauthorized
**Solution:** Check your Supabase API key is correct and not expired

### Issue: 404 Bucket Not Found
**Solution:** Verify bucket name matches the one in Supabase dashboard

### Issue: CORS Error
**Solution:** Add your frontend URL to allowed CORS origins in Supabase settings

### Issue: File Too Large
**Solution:** Increase file size limit in Supabase Storage settings (default 50MB on free tier)

## File URL Format

Public files will have URLs in this format:
```
https://YOUR_PROJECT_ID.supabase.co/storage/v1/object/public/YOUR_BUCKET_NAME/filename.jpg
```

## Testing with cURL

```bash
# Get your JWT token first
TOKEN="your-jwt-token-here"

# Upload a file
curl -X POST http://localhost:8000/users/upload-profile-picture \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/image.jpg"

# Response will include the file URL in the user object
```

## Maven Commands

```bash
# Clean and rebuild
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

## Links

- Supabase Dashboard: https://app.supabase.com
- Supabase Storage Docs: https://supabase.com/docs/guides/storage
- API Reference: https://supabase.com/docs/reference/javascript/storage
