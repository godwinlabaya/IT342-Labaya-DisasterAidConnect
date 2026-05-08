package edu.cit.labaya.disasteraidconnect.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserResponseDTO {

    private UUID          id;
    private String        username;
    private String        email;
    private String        profilePicture;
    private String        role;
    private OffsetDateTime createdAt;

    public static UserResponseDTO from(User u) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.id             = u.getId();
        dto.username       = u.getUsername();
        dto.email          = u.getEmail();
        dto.profilePicture = u.getProfilePicture();
        dto.role           = u.getRole();
        dto.createdAt      = u.getCreatedAt();
        return dto;
    }

    public UUID           getId()             { return id; }
    public String         getUsername()       { return username; }
    public String         getEmail()          { return email; }
    public String         getProfilePicture() { return profilePicture; }
    public String         getRole()           { return role; }
    public OffsetDateTime getCreatedAt()      { return createdAt; }
}