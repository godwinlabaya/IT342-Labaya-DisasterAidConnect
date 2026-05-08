package edu.cit.labaya.disasteraidconnect.disaster;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class DisasterRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String severityLevel = "Medium";
    private String status        = "Active";
    private Double latitude;
    private Double longitude;
    private UUID   createdBy;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;

    // ── Getters & Setters ──────────────────────────────────────────────────────

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

    public String getImageUrl1()                 { return imageUrl1; }
    public void   setImageUrl1(String u)         { this.imageUrl1 = u; }

    public String getImageUrl2()                 { return imageUrl2; }
    public void   setImageUrl2(String u)         { this.imageUrl2 = u; }

    public String getImageUrl3()                 { return imageUrl3; }
    public void   setImageUrl3(String u)         { this.imageUrl3 = u; }
}