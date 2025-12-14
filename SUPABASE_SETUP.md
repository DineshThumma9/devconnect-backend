# Supabase Storage Setup Guide

This guide will help you configure Supabase Storage for your application.

## Prerequisites

1. Create a Supabase account at [https://supabase.com](https://supabase.com)
2. Create a new project in Supabase

## Step 1: Get Your Supabase Credentials

1. Go to your Supabase project dashboard
2. Navigate to **Settings** → **API**
3. Copy the following values:
   - **Project URL** (e.g., `https://xxxxxxxxxxxxx.supabase.co`)
   - **anon/public key** (starts with `eyJ...`)

## Step 2: Create a Storage Bucket

1. In your Supabase dashboard, navigate to **Storage**
2. Click **New bucket**
3. Enter a bucket name (e.g., `profile-pictures`)
4. Choose bucket visibility:
   - **Public** - Files are publicly accessible (recommended for profile pictures)
   - **Private** - Files require authentication
5. Click **Create bucket**

## Step 3: Configure Bucket Policies (for Public Bucket)

If you created a public bucket:

1. Click on your bucket name
2. Go to **Policies** tab
3. Add the following policies:

### Policy for Public Read Access
```sql
CREATE POLICY "Public Access"
ON storage.objects FOR SELECT
USING (bucket_id = 'your-bucket-name');
```

### Policy for Authenticated Upload
```sql
CREATE POLICY "Authenticated users can upload"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'your-bucket-name');
```

### Policy for Authenticated Delete
```sql
CREATE POLICY "Authenticated users can delete their own files"
ON storage.objects FOR DELETE
USING (bucket_id = 'your-bucket-name');
```

## Step 4: Update Application Configuration

Update your `application.properties` file with your Supabase credentials:

```properties
# Supabase Configuration
supabase.url=https://xxxxxxxxxxxxx.supabase.co
supabase.key=your-anon-key-here
supabase.storage.bucket=your-bucket-name
```

**Important**: For production, use environment variables instead of hardcoding credentials:

```properties
supabase.url=${SUPABASE_URL}
supabase.key=${SUPABASE_KEY}
supabase.storage.bucket=${SUPABASE_BUCKET}
```

## Step 5: Test the Integration

You can test file upload using the following endpoint (assuming you have it implemented):

```bash
curl -X POST http://localhost:8000/api/users/profile-picture \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

## File Upload Limits

Default Supabase file upload limits:
- **Free tier**: 50MB per file
- **Pro tier**: 5GB per file

To change limits:
1. Go to **Storage** → **Settings**
2. Adjust the **Upload file size limit**

## Security Best Practices

1. **Never commit your Supabase keys to version control**
2. Use environment variables for sensitive data
3. Implement proper authentication before allowing file uploads
4. Validate file types and sizes on the server side
5. Consider using Row Level Security (RLS) policies for better security

## Troubleshooting

### CORS Issues
If you encounter CORS errors, add your frontend URL to allowed origins:
1. Go to **Settings** → **API** → **CORS**
2. Add your frontend URL

### Authentication Errors
Make sure you're using the correct API key:
- Use **anon/public key** for client-side operations
- Use **service_role key** (with caution) only for server-side admin operations

### File Not Found
Ensure the bucket is public or you're using proper authentication for private buckets.

## Additional Resources

- [Supabase Storage Documentation](https://supabase.com/docs/guides/storage)
- [Supabase Storage API Reference](https://supabase.com/docs/reference/javascript/storage)
- [Storage Policies Guide](https://supabase.com/docs/guides/storage/security/access-control)
