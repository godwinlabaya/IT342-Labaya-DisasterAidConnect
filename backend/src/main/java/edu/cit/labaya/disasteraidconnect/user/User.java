package edu.cit.labaya.disasteraidconnect.user;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;               // Mirrors auth.users — no generation

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "profile_picture", columnDefinition = "TEXT")
    private String profilePicture;

    @Column(name = "profile_picture_mime_type")
    private String profilePictureMimeType;

    @Column(name = "security_question", nullable = false)
    private String securityQuestion;

    @Column(name = "security_answer", nullable = false)
    private String securityAnswer;

    @Column
    private String role = "user";

    @Column(name = "created_at", updatable = false,
            columnDefinition = "timestamp with time zone default now()")
    private OffsetDateTime createdAt;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId()                                  { return id; }
    public void setId(UUID id)                           { this.id = id; }

    public String getUsername()                          { return username; }
    public void   setUsername(String username)           { this.username = username; }

    public String getEmail()                             { return email; }
    public void   setEmail(String email)                 { this.email = email; }

    public String getProfilePicture()                    { return profilePicture; }
    public void   setProfilePicture(String p)            { this.profilePicture = p; }

    public String getProfilePictureMimeType()            { return profilePictureMimeType; }
    public void   setProfilePictureMimeType(String m)    { this.profilePictureMimeType = m; }

    public String getSecurityQuestion()                  { return securityQuestion; }
    public void   setSecurityQuestion(String q)          { this.securityQuestion = q; }

    public String getSecurityAnswer()                    { return securityAnswer; }
    public void   setSecurityAnswer(String a)            { this.securityAnswer = a; }

    public String getRole()                              { return role; }
    public void   setRole(String role)                   { this.role = role; }

    public OffsetDateTime getCreatedAt()                 { return createdAt; }
    public void           setCreatedAt(OffsetDateTime t) { this.createdAt = t; }
}