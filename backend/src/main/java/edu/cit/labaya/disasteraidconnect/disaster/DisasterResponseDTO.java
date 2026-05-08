package edu.cit.labaya.disasteraidconnect.disaster;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DisasterResponseDTO {

    private UUID          id;
    private String        title;
    private String        description;
    private String        severityLevel;
    private String        status;
    private Double        latitude;
    private Double        longitude;
    private UUID          createdBy;
    private String        imageUrl1;
    private String        imageUrl2;
    private String        imageUrl3;
    private OffsetDateTime createdAt;

    // ── Factory ────────────────────────────────────────────────────────────────

    public static DisasterResponseDTO from(Disaster d) {
        DisasterResponseDTO dto = new DisasterResponseDTO();
        dto.id            = d.getId();
        dto.title         = d.getTitle();
        dto.description   = d.getDescription();
        dto.severityLevel = d.getSeverityLevel();
        dto.status        = d.getStatus();
        dto.latitude      = d.getLatitude();
        dto.longitude     = d.getLongitude();
        dto.createdBy     = d.getCreatedBy();
        dto.imageUrl1     = d.getImageUrl1();
        dto.imageUrl2     = d.getImageUrl2();
        dto.imageUrl3     = d.getImageUrl3();
        dto.createdAt     = d.getCreatedAt();
        return dto;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public UUID           getId()            { return id; }
    public String         getTitle()         { return title; }
    public String         getDescription()   { return description; }
    public String         getSeverityLevel() { return severityLevel; }
    public String         getStatus()        { return status; }
    public Double         getLatitude()      { return latitude; }
    public Double         getLongitude()     { return longitude; }
    public UUID           getCreatedBy()     { return createdBy; }
    public String         getImageUrl1()     { return imageUrl1; }
    public String         getImageUrl2()     { return imageUrl2; }
    public String         getImageUrl3()     { return imageUrl3; }
    public OffsetDateTime getCreatedAt()     { return createdAt; }
}