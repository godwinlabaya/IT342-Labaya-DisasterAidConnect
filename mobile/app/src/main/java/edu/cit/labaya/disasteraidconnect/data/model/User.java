package edu.cit.labaya.disasteraidconnect.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String username;
    private String email;
    private String role;             // "user" | "admin"

    @SerializedName("is_muted")
    private boolean isMuted;

    @SerializedName("mute_until")
    private String muteUntil;

    @SerializedName("mute_reason")
    private String muteReason;

    @SerializedName("profile_picture")
    private String profilePicture;

    @SerializedName("security_question")
    private String securityQuestion;

    @SerializedName("created_at")
    private String createdAt;

    public User() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }
    public String getMuteUntil() { return muteUntil; }
    public void setMuteUntil(String muteUntil) { this.muteUntil = muteUntil; }
    public String getMuteReason() { return muteReason; }
    public void setMuteReason(String muteReason) { this.muteReason = muteReason; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public boolean isAdmin() { return "admin".equals(role); }
}
