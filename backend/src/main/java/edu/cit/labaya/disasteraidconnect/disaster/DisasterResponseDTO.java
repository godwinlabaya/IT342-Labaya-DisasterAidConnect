package edu.cit.labaya.disasteraidconnect.dto;

import java.time.Instant;

import edu.cit.labaya.disasteraidconnect.entity.Disaster;

public class DisasterResponseDTO {

    private String  id;
    private String  title;
    private String  description;
    private String  severityLevel;
    private String  status;
    private Double  latitude;
    private Double  longitude;
    private String  createdBy;
    private Instant createdAt;
    private Instant updatedAt;

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
        dto.createdAt     = d.getCreatedAt();
        dto.updatedAt     = d.getUpdatedAt();
        return dto;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public String  getId()            { return id; }
    public String  getTitle()         { return title; }
    public String  getDescription()   { return description; }
    public String  getSeverityLevel() { return severityLevel; }
    public String  getStatus()        { return status; }
    public Double  getLatitude()      { return latitude; }
    public Double  getLongitude()     { return longitude; }
    public String  getCreatedBy()     { return createdBy; }
    public Instant getCreatedAt()     { return createdAt; }
    public Instant getUpdatedAt()     { return updatedAt; }
}