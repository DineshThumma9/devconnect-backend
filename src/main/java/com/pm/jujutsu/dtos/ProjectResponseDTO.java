package com.pm.jujutsu.dtos;

import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Data
public class ProjectResponseDTO {



    private String id;
    private String title;
    private String description;
    private Set<String> techRequirements = new HashSet<>();
    private String ownerUsername;
    private String ownerProfilePicUrl;
    private boolean isPrivate;
    private String status;
    private String ownerId;
    private Set<String> currentContributors = new HashSet<>();
    private String githubLink;
    private Date createdAt;
    private Set<String> media = new HashSet<>();




}


/*

Lets talk about whole workflow for a minuate here
now I am a user
I Login/Sign Up into the application
      1.Using Oauth
    2.Using email and password
Both of which are'nt tested properly here and have no gurantee so i need to check that first

I Ping certain interests to help in cold start
Interest i have pinged realted to that i will get:
    1.Projects
    2.Users
    3.Posts
Now when User post a new post or a project how will i get it
   1.Polling
   2.Websockets
    3.Push Notifications
How will that work auto refresh or manual refresh
I haven't Implemented it yet

Say am a User i create a post
Now Post will contain 
    1.Title
    2.Content
    3.Media
    4.Tags
    5.Likes,Comments,Shares all initially 0
    6.CreatedAt
    7.UpdatedAt
    8.Author Details (Username,ProfilePicUrl,UserId)
Now Point is where should i create UUID should i create on frontend and sent it to backend
    Or should backend create it
Best practice is backend should create it to avoid collisons and duplicates
Or not

Next same with Project or User UUID should backend create it or frontend
Now all i modeled around backend creating UUIDs
So may be we shoyld keep it that way


Now thing is interface and how things look on it
Once user logins He gets all projects,post and users
now point is how will i get that
    1.Pagination
    2.Infinite Scroll
    3.Load More Button
Now also 
When User get these as ResponseDTOs should i send UUID with it
if i send UUID frontend can easily fetch more details about that entity
But am i not leaking some information
else say i dont how wil i get a response body when user clicks on a project or post or user


Should Comments be of a string or an object

Should i make username and email as unique fields in UserRequestDTO
so i can fetch these without any issues
and i have to make post and project some attributes unique as well
what wil that be?
If i make project title unique then two users cant have same project title
That that would lead to complications
as project title need to be simple and user perference is simple project title

*/