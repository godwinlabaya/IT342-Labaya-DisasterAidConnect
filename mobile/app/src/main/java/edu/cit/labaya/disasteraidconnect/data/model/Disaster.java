package edu.cit.labaya.disasteraidconnect.data.model;

import com.google.gson.annotations.SerializedName;

public class Disaster {

    private String id;
    private String title;
    private String description;

    @SerializedName("severity_level")
    private String severityLevel;

    private String status;
    private Double latitude;
    private Double longitude;

    @SerializedName("created_by")
    private String createdBy;

    // userId alias — same field, for compatibility
    @SerializedName("user_id")
    private String userId;

    @SerializedName("image_url_1")
    private String imageUrl1;
    @SerializedName("image_url_2")
    private String imageUrl2;
    @SerializedName("image_url_3")
    private String imageUrl3;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("gcash_number")
    private String gcashNumber;

    // Set manually from join
    private String creatorUsername;
    private String reportedBy;
    // severity alias for admin code compatibility
    private String severity;

    public Disaster() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
        this.severity = severityLevel;
    }

    /** Works for both Spring Boot (severityLevel) and Supabase direct (severity_level) */
    public String getSeverity() {
        return severity != null ? severity : severityLevel;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
        this.severityLevel = severity;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getLatitude() { return latitude != null ? latitude : 0.0; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude != null ? longitude : 0.0; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    /** getUserId returns createdBy — same field, different name for compatibility */
    public String getUserId() { return userId != null ? userId : createdBy; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getImageUrl1() { return imageUrl1; }
    public void setImageUrl1(String imageUrl1) { this.imageUrl1 = imageUrl1; }
    public String getImageUrl2() { return imageUrl2; }
    public void setImageUrl2(String imageUrl2) { this.imageUrl2 = imageUrl2; }
    public String getImageUrl3() { return imageUrl3; }
    public void setImageUrl3(String imageUrl3) { this.imageUrl3 = imageUrl3; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getGcashNumber() { return gcashNumber; }
    public void setGcashNumber(String gcashNumber) { this.gcashNumber = gcashNumber; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public int getSeverityColor() {
        String s = getSeverity();
        if (s == null) return 0xFF3B82F6;
        switch (s) {
            case "Critical": return 0xFF7C3AED;
            case "High":     return 0xFFEF4444;
            case "Medium":   return 0xFFF97316;
            default:         return 0xFF3B82F6;
        }
    }
}