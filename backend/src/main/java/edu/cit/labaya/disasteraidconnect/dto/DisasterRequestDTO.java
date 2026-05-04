package edu.cit.labaya.disasteraidconnect.dto;

import jakarta.validation.constraints.NotBlank;

public class DisasterRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String severityLevel = "Medium";
    private String status        = "Active";
    private Double latitude;
    private Double longitude;
    private String createdBy;   

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

    public String getCreatedBy()                 { return createdBy; }
    public void   setCreatedBy(String uid)       { this.createdBy = uid; }
}