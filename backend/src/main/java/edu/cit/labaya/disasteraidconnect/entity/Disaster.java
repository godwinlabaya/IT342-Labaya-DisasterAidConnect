package edu.cit.labaya.disasteraidconnect.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "disasters")
public class Disaster {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "severity_level", nullable = false)
    private String severityLevel = "Low";   // Low | Medium | High | Critical

    @Column(nullable = false)
    private String status = "Active";       // Active | Monitoring | Resolved

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;                 // FK → users(id), nullable

    @Column(name = "created_at", updatable = false,
            columnDefinition = "timestamp with time zone default now()")
    private OffsetDateTime createdAt;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId()                          { return id; }
    public void setId(UUID id)                   { this.id = id; }

    public String getTitle()                     { return title; }
    public void   setTitle(String title)         { this.title = title; }

    public String getDescription()               { return description; }
    public void   setDescription(String d)       { this.description = d; }

    public String getSeverityLevel()             { return severityLevel; }
    public void   setSeverityLevel(String s)     { this.severityLevel = s; }

    public String getStatus()                    { return status; }
    public void   setStatus(String status)       { this.status = status; }

    public Double getLatitude()                  { return latitude; }
    public void   setLatitude(Double lat)        { this.latitude = lat; }

    public Double getLongitude()                 { return longitude; }
    public void   setLongitude(Double lng)       { this.longitude = lng; }

    public UUID getCreatedBy()                   { return createdBy; }
    public void setCreatedBy(UUID uid)           { this.createdBy = uid; }

    public OffsetDateTime getCreatedAt()         { return createdAt; }
    public void           setCreatedAt(OffsetDateTime t) { this.createdAt = t; }
}