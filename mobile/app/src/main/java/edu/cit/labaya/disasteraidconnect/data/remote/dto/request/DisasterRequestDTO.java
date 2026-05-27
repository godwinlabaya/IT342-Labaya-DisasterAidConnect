package edu.cit.labaya.disasteraidconnect.data.remote.dto.request;

import com.google.gson.annotations.SerializedName;

public class DisasterRequestDTO {

    private String title;
    private String description;

    @SerializedName("severityLevel")
    private String severityLevel = "Medium";   // Low | Medium | High | Critical

    private String status = "Active";          // Active | Monitoring | Resolved

    private Double latitude;
    private Double longitude;

    @SerializedName("createdBy")
    private String createdBy;   // UUID as string — Spring Boot accepts UUID from JSON string

    @SerializedName("imageUrl1")
    private String imageUrl1;

    @SerializedName("imageUrl2")
    private String imageUrl2;

    @SerializedName("imageUrl3")
    private String imageUrl3;

    public DisasterRequestDTO() {}

    public DisasterRequestDTO(String title, String description, String severityLevel,
                               String status, Double latitude, Double longitude,
                               String createdBy) {
        this.title         = title;
        this.description   = description;
        this.severityLevel = severityLevel;
        this.status        = status;
        this.latitude      = latitude;
        this.longitude     = longitude;
        this.createdBy     = createdBy;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getImageUrl1() { return imageUrl1; }
    public void setImageUrl1(String imageUrl1) { this.imageUrl1 = imageUrl1; }
    public String getImageUrl2() { return imageUrl2; }
    public void setImageUrl2(String imageUrl2) { this.imageUrl2 = imageUrl2; }
    public String getImageUrl3() { return imageUrl3; }
    public void setImageUrl3(String imageUrl3) { this.imageUrl3 = imageUrl3; }
}
